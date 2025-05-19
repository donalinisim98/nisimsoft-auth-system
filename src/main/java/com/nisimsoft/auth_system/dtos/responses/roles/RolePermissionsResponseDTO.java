package com.nisimsoft.auth_system.dtos.responses.roles;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "name", "description", "permissions" })
public record RolePermissionsResponseDTO(
                Long id,
                String name,
                String description,
                List<PermissionResponseDTO> roles) {

}
