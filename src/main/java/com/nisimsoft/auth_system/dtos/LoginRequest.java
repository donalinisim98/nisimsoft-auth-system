package com.nisimsoft.auth_system.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

  @NotBlank(message = "El email es obligatorio")
  @Email(message = "Formato de email inválido")
  private String email;

  @NotBlank(message = "La contraseña es obligatoria")
  private String password;
}
