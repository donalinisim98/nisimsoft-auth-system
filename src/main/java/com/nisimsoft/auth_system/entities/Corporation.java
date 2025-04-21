package com.nisimsoft.auth_system.entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nisimsoft.auth_system.entities.enums.NSCorpDBEngineEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "ns_corp")
@Data // Genera getters, setters, equals, hashCode
public class Corporation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gen")
    @SequenceGenerator(name = "seq_gen", sequenceName = "ns_corp_id_seq", allocationSize = 1)
    private Long id;

    @Column
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String name;

    @Column
    private String username;

    @Column
    private String password;

    @Column(nullable = true)
    private String logo;

    @Column(unique = true)
    private String host;

    @Column
    @Enumerated(EnumType.STRING)
    private NSCorpDBEngineEnum dbEngine = NSCorpDBEngineEnum.MSSQL;

    @ManyToMany(mappedBy = "corporations")
    @JsonIgnoreProperties("corporations") // 🔁 Evita que cada Corporation serialice a todos los Users
    private Set<User> users = new HashSet<>();
}
