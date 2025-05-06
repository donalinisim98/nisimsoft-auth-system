package com.nisimsoft.auth_system.dtos.requests;

import com.nisimsoft.auth_system.entities.enums.PermissionTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SavePermissionRequest {

    @NotBlank(message = "El nombre del permiso es obligatorio")
    private String name;

    @NotBlank(message = "El valor del permiso es obligatorio")
    @Pattern(regexp = "^[a-z]+(_[a-z]+)*$", message = "El valor debe estar en formato snake_case, solo letras minúsculas y guiones bajos simples")
    private String value;

    @NotNull(message = "El tipo de permiso es obligatorio")
    private PermissionTypeEnum type;
}
