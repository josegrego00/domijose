package com.domicilio.domijose.repositories;

import com.domicilio.domijose.models.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
    List<MetodoPago> findByActivoTrue();
}