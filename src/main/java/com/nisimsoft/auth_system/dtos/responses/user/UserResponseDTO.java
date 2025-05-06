package com.nisimsoft.auth_system.dtos.responses.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "username", "email", "corporations" })
public record UserResponseDTO(
                String name,
                String username,
                String email,
                List<CorporationResponseDTO> corporations) {
}