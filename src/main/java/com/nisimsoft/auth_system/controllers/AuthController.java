package com.nisimsoft.auth_system.controllers;

import com.nisimsoft.auth_system.config.JwtUtils;
import com.nisimsoft.auth_system.dtos.LoginRequest;
import com.nisimsoft.auth_system.dtos.RegisterRequest;
import com.nisimsoft.auth_system.entities.User;
import com.nisimsoft.auth_system.exceptions.auth.AuthenticationFailedException;
import com.nisimsoft.auth_system.responses.Response;
import com.nisimsoft.auth_system.services.AuthProviderFactory;
import com.nisimsoft.auth_system.services.AuthenticationService;
import com.nisimsoft.auth_system.services.providers.AuthenticationProvider;
import jakarta.validation.Valid;
import java.util.Map;
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
  @Autowired private AuthenticationService authService;

  @Autowired private AuthProviderFactory authProviderFactory;

  @Autowired private JwtUtils jwtUtils;

  @Value("${app.auth.provider}")
  private String activeAuthProvider;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

    Map<String, Object> data =
        Map.of(
            "name", request.getName(),
            "username", request.getUsername(),
            "email", request.getEmail(),
            "password", request.getPassword());
    // Registrar usuario
    User user = authService.registerUser(data);

    return new Response(
        "Usuario registrado exitosamente", Map.of("email", user.getEmail()), HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    AuthenticationProvider provider = authProviderFactory.getProvider(activeAuthProvider);

    if (provider == null) {
      throw new AuthenticationFailedException("Proveedor de autenticación no configurado");
    }

    boolean isAuthenticated = provider.authenticate(request.getEmail(), request.getPassword());

    if (!isAuthenticated) {
      throw new AuthenticationFailedException("Credenciales inválidas");
    }

    String token = jwtUtils.generateToken(request.getEmail());

    return new Response("Autenticación exitosa", Map.of("token", token), HttpStatus.OK);
  }
}
