package com.nisimsoft.auth_system.services;

import java.util.HashSet;

import java.util.List;
import org.springframework.stereotype.Service;

import com.nisimsoft.auth_system.dtos.requests.SaveRoleRequest;
import com.nisimsoft.auth_system.entities.Permission;
import com.nisimsoft.auth_system.entities.Role;
import com.nisimsoft.auth_system.repositories.PermissionRepository;
import com.nisimsoft.auth_system.repositories.RoleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public Role saveOrUpdateRole(SaveRoleRequest request) {

        Role role = roleRepository.findByName(request.getName())
                .orElseGet(Role::new); // Si no existe, crea uno nuevo

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        List<Long> permissionIds = request.getPermissions();
        // Buscar los permisos por ID
        List<Permission> foundPermissions = permissionRepository.findAllById(permissionIds);

        if (foundPermissions.size() != request.getPermissions().size()) {
            throw new IllegalArgumentException("Uno o más permisos no existen en el sistema");
        }

        // Actualizar el set de permisos
        role.setPermissions(new HashSet<>(foundPermissions));

        return roleRepository.save(role);
    }
}
