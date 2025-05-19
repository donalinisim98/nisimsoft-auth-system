package com.nisimsoft.auth_system.controllers;

import com.nisimsoft.auth_system.dtos.requests.LoginRequest;
import com.nisimsoft.auth_system.dtos.requests.RegisterUserRequest;
import com.nisimsoft.auth_system.dtos.requests.VerifyUserRequest;
import com.nisimsoft.auth_system.dtos.responses.user.CorporationResponseDTO;
import com.nisimsoft.auth_system.dtos.responses.user.UserResponseDTO;
import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.User;
import com.nisimsoft.auth_system.exceptions.auth.AuthenticationFailedException;
import com.nisimsoft.auth_system.responses.Response;
import com.nisimsoft.auth_system.services.AuthProviderFactory;
import com.nisimsoft.auth_system.services.AuthenticationService;
import com.nisimsoft.auth_system.services.providers.AuthenticationProvider;
import com.nisimsoft.auth_system.utils.JwtUtils;

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

@RestController
@RequestMapping("/api")
public class AuthController {
  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private AuthProviderFactory authProviderFactory;

  @Autowired
  private JwtUtils jwtUtils;

  @Value("${app.auth.provider}")
  private String activeAuthProvider;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest request) {

    // Registrar usuario
    User user = authenticationService.registerUser(request);

    // IMPORTANTE: crea una copia segura del set (para evitar que JPA lo gestione al
    // recorrer)
    Set<Corporation> safeCorporations = new HashSet<>(user.getCorporations());

    // Convertir corporaciones a resumen DTO
    List<CorporationResponseDTO> corporationDTOs = safeCorporations.stream()
        .map(corp -> new CorporationResponseDTO(corp.getId(), corp.getName()))
        .toList();

    UserResponseDTO responseDTO = new UserResponseDTO(
        user.getId(),
        user.getName(),
        user.getUsername(),
        user.getEmail(),
        corporationDTOs);

    return new Response(
        "Usuario registrado exitosamente", responseDTO, HttpStatus.CREATED);
  }

  @PostMapping("/verify-user")
  public ResponseEntity<?> verifyUser(@Valid @RequestBody VerifyUserRequest request) {
    AuthenticationProvider provider = getAuthenticationProvider();

    User user = authenticationService.getUserByEmailOrThrow(request.getEmail());

    authenticationService.authenticateOrThrow(provider, request.getEmail(), request.getPassword());

    Set<Corporation> safeCorporations = new HashSet<>(user.getCorporations());
    List<CorporationResponseDTO> corporationDTOs = mapCorporations(safeCorporations);

    UserResponseDTO responseDTO = new UserResponseDTO(
        user.getId(),
        user.getName(),
        user.getUsername(),
        user.getEmail(),
        corporationDTOs);

    return new Response("Usuario encontrado exitosamente", responseDTO, HttpStatus.OK);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    AuthenticationProvider provider = getAuthenticationProvider();

    User user = authenticationService.getUserByIdOrThrow(request.getUserId());

    authenticationService.validateUserBelongsToCorporation(user, request.getCorpId());

    authenticationService.authenticateOrThrow(provider, user.getEmail(), request.getPassword());

    String token = jwtUtils.generateToken(user.getEmail(), request.getCorpId().toString());

    return new Response("Autenticación exitosa", Map.of("token", token), HttpStatus.OK);
  }

  private AuthenticationProvider getAuthenticationProvider() {
    AuthenticationProvider provider = authProviderFactory.getProvider(activeAuthProvider);
    if (provider == null) {
      throw new AuthenticationFailedException("Proveedor de autenticación no configurado");
    }
    return provider;
  }

  private List<CorporationResponseDTO> mapCorporations(Set<Corporation> corporations) {
    return corporations.stream()
        .map(corp -> new CorporationResponseDTO(corp.getId(), corp.getName()))
        .toList();
  }
}
