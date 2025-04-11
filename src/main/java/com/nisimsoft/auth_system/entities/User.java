package com.nisimsoft.auth_system.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity // Mapea la clase a una tabla SQL
@Table(name = "ns_users") // Evita conflicto con "user"
@Data // Genera getters, setters, equals, hashCode
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gen")
  @SequenceGenerator(name = "seq_gen", sequenceName = "ns_users_id_seq", allocationSize = 50)
  private Long id;

  @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
  @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
  private String name;

  @Column(unique = true)
  @NotBlank(message = "El nombre de usuario es obligatorio")
  @Size(min = 3, message = "El nombre de usuario debe tener al menos 3 caracteres")
  @Size(max = 50, message = "El nombre de usuario no puede exceder los 50 caracteres")
  private String username;

  @Column(unique = true)
  @NotBlank(message = "El campo correo es obligatorio")
  @Email(message = "Formato de email inválido")
  private String email;

  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
  private String password;
}
