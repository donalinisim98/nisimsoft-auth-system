package com.nisimsoft.auth_system.services;

import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.User;
import com.nisimsoft.auth_system.exceptions.auth.EmailAlreadyExistsException;
import com.nisimsoft.auth_system.repositories.CorporationRepository;
import com.nisimsoft.auth_system.repositories.UserRepository;

import java.util.HashSet;
import java.util.Map;
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

  public User registerUser(Map<String, Object> userData) {

    String email = (String) userData.get("email");
    String name = (String) userData.get("name");
    String username = (String) userData.get("username");
    String password = (String) userData.get("password");
    Object corporationIds = userData.get("corporationIds");

    // Verificar si el email ya existe
    if (userRepository.findByEmail(email).isPresent()) {
      throw new EmailAlreadyExistsException("El email ya está registrado");
    }

    // Crear y guardar el usuario
    User user = new User();
    user.setName(name);
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));

    // ✅ Verificar si el objeto es iterable (ej. List, Set, etc.)
    if (corporationIds instanceof Iterable<?> ids) {
      Set<Long> corpIds = new HashSet<>();

      for (Object id : ids) {
        corpIds.add(Long.valueOf(id.toString()));
      }

      // ✅ Obtener las entidades de la base
      Set<Corporation> corporations = new HashSet<>(corporationRepository.findAllById(corpIds));
      user.setCorporations(corporations);
    }

    return userRepository.save(user);
  }
}
