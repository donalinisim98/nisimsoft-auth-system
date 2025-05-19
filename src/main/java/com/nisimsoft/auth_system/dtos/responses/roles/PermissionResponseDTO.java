package com.nisimsoft.auth_system.dtos.responses.roles;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nisimsoft.auth_system.entities.enums.PermissionTypeEnum;

@JsonPropertyOrder({ "id", "name", "value", "type" })
public record PermissionResponseDTO(Long id, String name, String value, PermissionTypeEnum type) {

}
