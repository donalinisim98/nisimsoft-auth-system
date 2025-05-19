package com.nisimsoft.auth_system.services;

import org.springframework.stereotype.Service;

import com.nisimsoft.auth_system.dtos.requests.SavePermissionRequest;
import com.nisimsoft.auth_system.entities.Permission;
import com.nisimsoft.auth_system.repositories.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public Permission savePermission(SavePermissionRequest request) {
        Permission permission = new Permission();

        permission.setName(request.getName());
        permission.setValue(request.getValue());
        permission.setType(request.getType());

        return permissionRepository.save(permission);
    }
}
