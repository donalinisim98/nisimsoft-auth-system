package com.nisimsoft.auth_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nisimsoft.auth_system.entities.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

}
