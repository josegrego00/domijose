package com.josepinodev.appdomirest.repository;

import com.josepinodev.appdomirest.model.EVentaEstado;
import com.josepinodev.appdomirest.model.VentaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<VentaEntity, Long> {

    List<VentaEntity> findByUsuarioIdAndActivoTrue(Long usuarioId);

    Page<VentaEntity> findByUsuarioIdAndActivoTrue(Long usuarioId, Pageable pageable);

    Page<VentaEntity> findByUsuarioIdAndActivoTrueAndFechaBetween(Long usuarioId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<VentaEntity> findByActivoTrue(Pageable pageable);

    Page<VentaEntity> findByEstadoAndActivoTrue(EVentaEstado estado, Pageable pageable);

    long countByEstadoAndActivoTrue(EVentaEstado estado);
}