package com.ubu.pokedex.model;

import jakarta.persistence.*;

// Entidad JPA que mapea la tabla "users" en la base de datos PostgreSQL
@Entity
@Table(name = "users")
public class User {

    // Clave primaria autoincremental
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de usuario (debe ser único)
    @Column(unique = true, nullable = false)
    private String username;

    // Contraseña en texto plano (en un entorno de producción debería estar encriptada)
    @Column(nullable = false)
    private String password;

    // Rol del usuario (ej. ADMIN, USER)
    @Column(nullable = false)
    private String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
