package com.josepinodev.appdomirest.repository;

import com.josepinodev.appdomirest.model.RutaDomicilioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaDomicilioRepository extends JpaRepository<RutaDomicilioEntity, Long> {

    List<RutaDomicilioEntity> findByActivo(Boolean activo);

    List<RutaDomicilioEntity> findByBarrioContainingIgnoreCase(String barrio);
}