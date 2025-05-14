package com.nisimsoft.auth_system.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

  @NotNull(message = "El id del usuario es obligatorio")
  @Min(value = 1, message = "El ID del usuario debe ser mayor que 0")
  private Long userId;

  @NotBlank(message = "La contraseña es obligatoria")
  private String password;

  @NotNull(message = "El ID de la corporación es obligatorio")
  @Min(value = 1, message = "El ID de la corporación debe ser mayor que 0")
  private Long corpId;
}
