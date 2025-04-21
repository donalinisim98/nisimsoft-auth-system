package com.nisimsoft.auth_system.controllers;

import com.nisimsoft.auth_system.config.JwtUtils;
import com.nisimsoft.auth_system.dtos.requests.LoginRequest;
import com.nisimsoft.auth_system.dtos.requests.RegisterRequest;
import com.nisimsoft.auth_system.dtos.responses.user.CorporationSummaryDTO;
import com.nisimsoft.auth_system.dtos.responses.user.UserResponseDTO;
import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.User;
import com.nisimsoft.auth_system.exceptions.auth.AuthenticationFailedException;
import com.nisimsoft.auth_system.repositories.CorporationRepository;
import com.nisimsoft.auth_system.responses.Response;
import com.nisimsoft.auth_system.services.AuthProviderFactory;
import com.nisimsoft.auth_system.services.AuthenticationService;
import com.nisimsoft.auth_system.services.providers.AuthenticationProvider;
import jakarta.validation.Valid;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthController {
  @Autowired
  private AuthenticationService authService;

  @Autowired
  private AuthProviderFactory authProviderFactory;

  @Autowired
  private CorporationRepository corporationRepository;

  @Autowired
  private JwtUtils jwtUtils;

  @Value("${app.auth.provider}")
  private String activeAuthProvider;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

    // Registrar usuario
    User user = authService.registerUser(request);

    // IMPORTANTE: crea una copia segura del set (para evitar que JPA lo gestione al
    // recorrer)
    Set<Corporation> safeCorporations = new HashSet<>(user.getCorporations());

    // ✅ Convertir corporaciones a resumen DTO
    List<CorporationSummaryDTO> corporationDTOs = safeCorporations.stream()
        .map(corp -> new CorporationSummaryDTO(corp.getId(), corp.getName()))
        .toList();

    UserResponseDTO responseDTO = new UserResponseDTO(
        user.getName(),
        user.getUsername(),
        user.getEmail(),
        corporationDTOs);

    return new Response(
        "Usuario registrado exitosamente", responseDTO, HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

    corporationRepository.findById(request.getCorpId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Corporación no encontrada"));

    AuthenticationProvider provider = authProviderFactory.getProvider(activeAuthProvider);

    if (provider == null) {
      throw new AuthenticationFailedException("Proveedor de autenticación no configurado");
    }

    boolean isAuthenticated = provider.authenticate(request.getEmail(), request.getPassword());

    if (!isAuthenticated) {
      throw new AuthenticationFailedException("Credenciales inválidas");
    }

    String token = jwtUtils.generateToken(request.getEmail(), request.getCorpId().toString());

    return new Response("Autenticación exitosa", Map.of("token", token), HttpStatus.OK);
  }
}
