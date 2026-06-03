package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.ruta.RutaDomicilioDTO;
import com.josepinodev.appdomirest.mapper.RutaDomicilioMapper;
import com.josepinodev.appdomirest.model.RutaDomicilioEntity;
import com.josepinodev.appdomirest.repository.RutaDomicilioRepository;
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
public class RutaDomicilioService {

    private final RutaDomicilioRepository rutaDomicilioRepository;
    private final RutaDomicilioMapper rutaDomicilioMapper;
    private final AuditService auditService;

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<RutaDomicilioDTO> findAll() {
        return rutaDomicilioRepository.findAll().stream()
                .map(rutaDomicilioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    public List<RutaDomicilioDTO> findAllActive() {
        return rutaDomicilioRepository.findByActivo(true).stream()
                .map(rutaDomicilioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    public Optional<RutaDomicilioDTO> findById(Long id) {
        return rutaDomicilioRepository.findById(id)
                .map(rutaDomicilioMapper::toDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<RutaDomicilioDTO> findByBarrioContaining(String barrio) {
        return rutaDomicilioRepository.findByBarrioContainingIgnoreCase(barrio).stream()
                .map(rutaDomicilioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public RutaDomicilioDTO save(RutaDomicilioDTO dto) {
        RutaDomicilioEntity entity = rutaDomicilioMapper.toEntity(dto);
        RutaDomicilioDTO saved = rutaDomicilioMapper.toDTO(rutaDomicilioRepository.save(entity));
        auditService.log("CREAR_RUTA", "Ruta " + saved.getBarrio() + " creada");
        return saved;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public RutaDomicilioDTO update(RutaDomicilioDTO dto) {
        RutaDomicilioEntity existing = rutaDomicilioRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));

        dto.setFechaCreacion(existing.getFechaCreacion());

        RutaDomicilioEntity entity = rutaDomicilioMapper.toEntity(dto);
        entity.setId(existing.getId());
        RutaDomicilioDTO updated = rutaDomicilioMapper.toDTO(rutaDomicilioRepository.save(entity));
        auditService.log("EDITAR_RUTA", "Ruta #" + dto.getId() + ": " + updated.getBarrio() + " actualizada");
        return updated;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id) {
        rutaDomicilioRepository.findById(id).ifPresent(ruta -> {
            ruta.setActivo(false);
            rutaDomicilioRepository.save(ruta);
            log.info("Ruta eliminada (soft-delete): {}", ruta.getBarrio());
            auditService.log("ELIMINAR_RUTA", "Ruta #" + id + ": " + ruta.getBarrio() + " eliminada (soft-delete)");
        });
    }
}