package com.josepinodev.appdomirest.repository;

import com.josepinodev.appdomirest.model.CuentaBancoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaBancoRepository extends JpaRepository<CuentaBancoEntity, Long> {

    List<CuentaBancoEntity> findByActivoTrue();

    List<CuentaBancoEntity> findByActivo(boolean activo);
}
