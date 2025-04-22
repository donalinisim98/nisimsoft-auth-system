package com.nisimsoft.auth_system.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveCorpRequest {

    @NotBlank(message = "El motor de base es obligatorio")
    private String dbEngine;

    @NotBlank(message = "El motor de host es obligatorio")
    private String host;

    @NotBlank(message = "El usuario del motor de base de datos es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña para el motor de base de datos es obligatorio")
    private String password;
}