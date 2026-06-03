package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.cuenta.CuentaBancoDTO;
import com.josepinodev.appdomirest.mapper.CuentaBancoMapper;
import com.josepinodev.appdomirest.model.CuentaBancoEntity;
import com.josepinodev.appdomirest.repository.CuentaBancoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuentaBancoService {

    private final CuentaBancoRepository cuentaBancoRepository;
    private final CuentaBancoMapper cuentaBancoMapper;
    private final AuditService auditService;

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<CuentaBancoDTO> findAll() {
        return cuentaBancoRepository.findAll().stream()
                .map(cuentaBancoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    public List<CuentaBancoDTO> findAllActive() {
        return cuentaBancoRepository.findByActivoTrue().stream()
                .map(cuentaBancoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    public Optional<CuentaBancoDTO> findById(Long id) {
        return cuentaBancoRepository.findById(id)
                .map(cuentaBancoMapper::toDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public CuentaBancoDTO save(CuentaBancoDTO dto) {
        CuentaBancoEntity entity = cuentaBancoMapper.toEntity(dto);
        CuentaBancoDTO saved = cuentaBancoMapper.toDTO(cuentaBancoRepository.save(entity));
        auditService.log("CREAR_CUENTA_BANCO", "Cuenta " + saved.getBanco() + " - " + saved.getNumeroCuenta() + " creada");
        return saved;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public CuentaBancoDTO update(CuentaBancoDTO dto) {
        CuentaBancoEntity existing = cuentaBancoRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));

        dto.setFechaCreacion(existing.getFechaCreacion());

        CuentaBancoEntity entity = cuentaBancoMapper.toEntity(dto);
        entity.setId(existing.getId());
        CuentaBancoDTO updated = cuentaBancoMapper.toDTO(cuentaBancoRepository.save(entity));
        auditService.log("EDITAR_CUENTA_BANCO", "Cuenta #" + dto.getId() + ": " + updated.getBanco() + " actualizada");
        return updated;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id) {
        cuentaBancoRepository.findById(id).ifPresent(cuenta -> {
            cuenta.setActivo(false);
            cuentaBancoRepository.save(cuenta);
            log.info("Cuenta bancaria eliminada (soft-delete): {}", cuenta.getBanco());
            auditService.log("ELIMINAR_CUENTA_BANCO", "Cuenta #" + id + ": " + cuenta.getBanco() + " eliminada");
        });
    }
}
