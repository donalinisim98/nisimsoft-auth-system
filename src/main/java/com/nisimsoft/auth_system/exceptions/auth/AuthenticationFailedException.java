package com.nisimsoft.auth_system.exceptions.auth;

public class AuthenticationFailedException extends RuntimeException {
  public AuthenticationFailedException(String message) {
    super(message);
  }
}
