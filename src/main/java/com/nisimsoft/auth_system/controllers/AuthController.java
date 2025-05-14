package com.nisimsoft.auth_system.controllers;

import com.nisimsoft.auth_system.config.JwtUtils;
import com.nisimsoft.auth_system.dtos.requests.LoginRequest;
import com.nisimsoft.auth_system.dtos.requests.RegisterUserRequest;
import com.nisimsoft.auth_system.dtos.requests.SaveCorpRequest;
import com.nisimsoft.auth_system.dtos.requests.SavePermissionRequest;
import com.nisimsoft.auth_system.dtos.requests.UpdateCorpRequest;
import com.nisimsoft.auth_system.dtos.requests.VerifyUserRequest;
import com.nisimsoft.auth_system.dtos.responses.user.CorporationResponseDTO;
import com.nisimsoft.auth_system.dtos.responses.user.UserResponseDTO;
import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.Permission;
import com.nisimsoft.auth_system.entities.User;
import com.nisimsoft.auth_system.entities.enums.NSCorpDBEngineEnum;
import com.nisimsoft.auth_system.exceptions.auth.AuthenticationFailedException;
import com.nisimsoft.auth_system.repositories.CorporationRepository;
import com.nisimsoft.auth_system.repositories.UserRepository;
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
import org.springframework.web.bind.annotation.PutMapping;
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
  private UserRepository userRepository;

  @Autowired
  private JwtUtils jwtUtils;

  @Value("${app.auth.provider}")
  private String activeAuthProvider;

  @PostMapping("/corporation")
  public ResponseEntity<?> saveCorporation(@Valid @RequestBody SaveCorpRequest request) {

    Corporation corporation = new Corporation();

    corporation.setDbEngine(NSCorpDBEngineEnum.valueOf(request.getDbEngine()));
    corporation.setDbName(request.getDbName());
    corporation.setHost(request.getHost());
    corporation.setName(request.getName());
    corporation.setUsername(request.getUsername());
    corporation.setPassword(request.getPassword());

    corporationRepository.save(corporation);
    CorporationResponseDTO responseDTO = new CorporationResponseDTO(corporation.getId(), corporation.getName());

    return new Response(
        "Corporación guardada realizada exitosamente", responseDTO, HttpStatus.CREATED);
  }

  @PutMapping("/corporation")
  public ResponseEntity<?> updateCorporation(@Valid @RequestBody UpdateCorpRequest request) {

    Corporation corporation = corporationRepository.findById(request.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Corporación no encontrada"));

    corporation.setDbEngine(NSCorpDBEngineEnum.valueOf(request.getDbEngine()));
    corporation.setDbName(request.getDbName());
    corporation.setHost(request.getHost());
    corporation.setName(request.getName());
    corporation.setUsername(request.getUsername());
    corporation.setPassword(request.getPassword());

    corporationRepository.save(corporation);

    CorporationResponseDTO responseDTO = new CorporationResponseDTO(request.getId(), corporation.getName());

    return new Response(
        "Corporación actualizada realizada exitosamente", responseDTO, HttpStatus.OK);
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest request) {

    // Registrar usuario
    User user = authService.registerUser(request);

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

    User user = getUserByEmailOrThrow(request.getEmail());

    authenticateOrThrow(provider, request.getEmail(), request.getPassword());

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

    User user = getUserByIdOrThrow(request.getUserId());
    validateUserBelongsToCorporation(user, request.getCorpId());
    authenticateOrThrow(provider, user.getEmail(), request.getPassword());

    String token = jwtUtils.generateToken(user.getEmail(), request.getCorpId().toString());

    return new Response("Autenticación exitosa", Map.of("token", token), HttpStatus.OK);
  }

  @PostMapping("/permission")
  public ResponseEntity<?> savePermission(@Valid @RequestBody SavePermissionRequest request) {

    // Registrar permiso
    Permission permission = authService.savePermission(request);

    return new Response(
        "Permiso guardado exitosamente", Map.of("permission", permission), HttpStatus.CREATED);
  }

  private AuthenticationProvider getAuthenticationProvider() {
    AuthenticationProvider provider = authProviderFactory.getProvider(activeAuthProvider);
    if (provider == null) {
      throw new AuthenticationFailedException("Proveedor de autenticación no configurado");
    }
    return provider;
  }

  private User getUserByEmailOrThrow(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
  }

  private User getUserByIdOrThrow(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
  }

  private void authenticateOrThrow(AuthenticationProvider provider, String email, String password) {
    if (!provider.authenticate(email, password)) {
      throw new AuthenticationFailedException("Credenciales inválidas");
    }
  }

  private void validateUserBelongsToCorporation(User user, Long corpId) {
    boolean belongs = user.getCorporations()
        .stream()
        .anyMatch(c -> c.getId().equals(corpId));
    if (!belongs) {
      throw new AuthenticationFailedException("El usuario no pertenece a la corporación");
    }
  }

  private List<CorporationResponseDTO> mapCorporations(Set<Corporation> corporations) {
    return corporations.stream()
        .map(corp -> new CorporationResponseDTO(corp.getId(), corp.getName()))
        .toList();
  }
}
