package com.nisimsoft.auth_system.dtos.responses.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "name", "username", "email", "corporations" })
public record UserResponseDTO(
        Long id,
        String name,
        String username,
        String email,
        List<CorporationResponseDTO> corporations) {
}