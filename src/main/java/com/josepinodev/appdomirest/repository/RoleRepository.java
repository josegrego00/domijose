package com.josepinodev.appdomirest.repository;

import com.josepinodev.appdomirest.model.ERole;
import com.josepinodev.appdomirest.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(ERole name);

    boolean existsByName(ERole name);
}