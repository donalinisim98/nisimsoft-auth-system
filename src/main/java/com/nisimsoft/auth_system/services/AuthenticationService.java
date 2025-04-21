package com.nisimsoft.auth_system.services;

import com.nisimsoft.auth_system.dtos.requests.RegisterRequest;
import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.User;
import com.nisimsoft.auth_system.exceptions.auth.EmailAlreadyExistsException;
import com.nisimsoft.auth_system.repositories.CorporationRepository;
import com.nisimsoft.auth_system.repositories.UserRepository;

import java.util.HashSet;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CorporationRepository corporationRepository;

  public User registerUser(RegisterRequest request) {

    String email = request.getEmail();

    // Verificar si el email ya existe
    if (userRepository.findByEmail(email).isPresent()) {
      throw new EmailAlreadyExistsException("El email ya está registrado");
    }

    User user = new User();
    user.setName(request.getName());
    user.setUsername(request.getUsername());
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    Set<Long> corpIds = request.getCorporationIds();

    if (corpIds != null && !corpIds.isEmpty()) {

      Set<Corporation> corporations = new HashSet<>(corporationRepository.findAllById(corpIds));
      user.setCorporations(corporations);
    }

    return userRepository.save(user);
  }
}
