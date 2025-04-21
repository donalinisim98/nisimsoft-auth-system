package com.nisimsoft.auth_system.responses;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Response extends ResponseEntity<Object> {

  public Response(String message, Object data, HttpStatus status) {
    super(
        Map.of(
            "message", message,
            "data", data),
        status);
  }
}
