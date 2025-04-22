package com.nisimsoft.auth_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nisimsoft.auth_system.entities.Corporation;

public interface CorporationRepository extends JpaRepository<Corporation, Long> {
  // Aquí puedes agregar métodos personalizados si es necesario
  // Por ejemplo, encontrar una corporación por su nombre o ID
  // Optional<Corporation> findByName(String name);

}
