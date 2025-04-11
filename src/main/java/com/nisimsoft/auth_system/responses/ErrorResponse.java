package com.nisimsoft.auth_system.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
  private String error;
  private String message;
  private String path;
}
