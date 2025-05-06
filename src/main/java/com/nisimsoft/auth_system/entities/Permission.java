package com.nisimsoft.auth_system.entities;

import com.nisimsoft.auth_system.entities.enums.PermissionTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ns_permissions")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")

    private String name;

    @Column(unique = true)
    @Size(min = 3, message = "El valor del permiso del permiso debe tener al menos 3 caracteres")
    @Size(max = 50, message = "El valor del permiso del permiso no puede exceder los 50 caracteres")
    private String value;

    @Column
    @Enumerated(EnumType.STRING)
    private PermissionTypeEnum type;
}
