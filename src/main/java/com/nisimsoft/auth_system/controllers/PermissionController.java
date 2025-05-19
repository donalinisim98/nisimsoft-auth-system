package com.nisimsoft.auth_system.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nisimsoft.auth_system.dtos.requests.SavePermissionRequest;
import com.nisimsoft.auth_system.entities.Permission;
import com.nisimsoft.auth_system.responses.Response;
import com.nisimsoft.auth_system.services.PermissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/permission")
    public ResponseEntity<?> savePermission(@Valid @RequestBody SavePermissionRequest request) {

        // Registrar permiso
        Permission permission = permissionService.savePermission(request);

        return new Response(
                "Permiso guardado exitosamente", Map.of("permission", permission), HttpStatus.CREATED);
    }

}
