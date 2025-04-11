package com.nisimsoft.auth_system.services;

import com.nisimsoft.auth_system.entities.User;
import com.nisimsoft.auth_system.exceptions.auth.EmailAlreadyExistsException;
import com.nisimsoft.auth_system.repositories.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User registerUser(Map<String, Object> userData) {

    String email = (String) userData.get("email");
    String name = (String) userData.get("name");
    String username = (String) userData.get("username");
    String password = (String) userData.get("password");
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

    return userRepository.save(user);
  }
}
