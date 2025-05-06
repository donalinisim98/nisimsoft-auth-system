package com.nisimsoft.auth_system.entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nisimsoft.auth_system.entities.enums.NSCorpDBEngineEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ns_corp")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Corporation {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String name;

    @Column
    private String dbName;

    @Column
    private String username;

    @Column
    private String password;

    @Column(nullable = true)
    private String logo;

    private String host;

    @Column
    @Enumerated(EnumType.STRING)
    private NSCorpDBEngineEnum dbEngine = NSCorpDBEngineEnum.MSSQL;

    @ManyToMany(mappedBy = "corporations", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private Set<User> users = new HashSet<>();
}
