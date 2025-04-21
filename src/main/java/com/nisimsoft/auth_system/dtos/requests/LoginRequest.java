package com.nisimsoft.auth_system.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

  @NotBlank(message = "El email es obligatorio")
  @Email(message = "Formato de email inválido")
  private String email;

  @NotBlank(message = "La contraseña es obligatoria")
  private String password;

  @NotNull(message = "El ID de la corporación es obligatorio")
  @Min(value = 1, message = "El ID de la corporación debe ser mayor que 0")
  private Long corpId;
}
