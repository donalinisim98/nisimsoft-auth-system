package com.nisimsoft.auth_system.dtos.requests;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaveRoleRequest {
    @NotBlank(message = "El nombre del rol es obligatorio")
    private String name;

    @NotBlank(message = "La descripción del rol es obligatoria")
    private String description;

    @NotNull(message = "La lista de permisos no puede ser nula")
    @NotEmpty(message = "Debe proporcionar al menos un id de permiso")
    @Size(min = 1, message = "Debe haber al menos 1 id permiso")
    private List<@Positive(message = "Los IDs de permisos deben ser positivos") Long> permissions;
}
