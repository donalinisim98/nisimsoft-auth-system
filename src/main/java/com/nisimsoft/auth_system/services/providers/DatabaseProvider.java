package com.nisimsoft.auth_system.services.providers;

import com.nisimsoft.auth_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseProvider implements AuthenticationProvider {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public boolean authenticate(String email, String password) {
    return userRepository
        .findByEmail(email)
        .map(user -> passwordEncoder.matches(password, user.getPassword()))
        .orElse(false);
  }

  @Override
  public String getProviderName() {
    return "database";
  }
}
