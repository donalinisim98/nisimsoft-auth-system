package com.nisimsoft.auth_system.services.providers;

public interface AuthenticationProvider {
  boolean authenticate(String email, String password);

  String getProviderName();
}
