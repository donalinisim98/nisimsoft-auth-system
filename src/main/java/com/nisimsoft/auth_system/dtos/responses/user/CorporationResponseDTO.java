package com.nisimsoft.auth_system.dtos.responses.user;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "name" })
public record CorporationResponseDTO(Long id, String name) {
}
