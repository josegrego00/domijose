package com.josepinodev.appdomirest.repository;

import com.josepinodev.appdomirest.model.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    Optional<ProductoEntity> findByCodigo(String codigo);

    List<ProductoEntity> findByActivo(Boolean activo);

    Page<ProductoEntity> findByActivo(Boolean activo, Pageable pageable);
    
    Page<ProductoEntity> findAll(Pageable pageable);

    List<ProductoEntity> findByNombreContainingIgnoreCase(String nombre);

    Long countByActivo(Boolean activo);

    List<ProductoEntity> findByDisponible(Boolean disponible);

    Optional<ProductoEntity> findTopByOrderByCodigoDesc();
}