package com.ubu.pokedex.repository;

import com.ubu.pokedex.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositorio JPA para realizar operaciones de base de datos sobre la entidad User
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Busca un usuario concreto por su nombre de usuario (username)
    Optional<User> findByUsername(String username);
}
