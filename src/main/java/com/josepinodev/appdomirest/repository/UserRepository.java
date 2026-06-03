package com.josepinodev.appdomirest.repository;

import com.josepinodev.appdomirest.model.ERole;
import com.josepinodev.appdomirest.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByTelefono(String telefono);

    boolean existsByUsername(String username);

    boolean existsByTelefono(String telefono);

    boolean existsByEmail(String email);

    Long countByActivo(Boolean activo);

    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name = :role")
    List<UserEntity> findByRoles_Name(ERole role);

    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name = :role AND u.activo = :activo")
    List<UserEntity> findByRoles_NameAndActivo(ERole role, Boolean activo);

    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name = :role AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR u.telefono LIKE CONCAT('%', :query, '%'))")
    List<UserEntity> searchByUsernameOrTelefono(ERole role, String query);
}