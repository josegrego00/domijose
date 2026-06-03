package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.ruta.RutaDomicilioDTO;
import com.josepinodev.appdomirest.mapper.RutaDomicilioMapper;
import com.josepinodev.appdomirest.model.RutaDomicilioEntity;
import com.josepinodev.appdomirest.repository.RutaDomicilioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RutaDomicilioServiceTest {

    @Mock private RutaDomicilioRepository rutaDomicilioRepository;
    @Mock private RutaDomicilioMapper rutaDomicilioMapper;
    @Mock private AuditService auditService;
    @InjectMocks private RutaDomicilioService rutaDomicilioService;

    private RutaDomicilioEntity entity;
    private RutaDomicilioDTO dto;

    @BeforeEach
    void setUp() {
        entity = new RutaDomicilioEntity();
        entity.setId(1L);
        entity.setBarrio("Barrio Test");
        entity.setPrecio(new BigDecimal("5000"));
        entity.setActivo(true);

        dto = new RutaDomicilioDTO();
        dto.setId(1L);
        dto.setBarrio("Barrio Test");
        dto.setPrecio(new BigDecimal("5000"));
        dto.setActivo(true);
    }

    @Test
    void findAll_ReturnsAll() {
        when(rutaDomicilioRepository.findAll()).thenReturn(List.of(entity));
        when(rutaDomicilioMapper.toDTO(entity)).thenReturn(dto);

        var result = rutaDomicilioService.findAll();

        assertEquals(1, result.size());
        assertEquals("Barrio Test", result.get(0).getBarrio());
    }

    @Test
    void findAllActive_ReturnsOnlyActive() {
        when(rutaDomicilioRepository.findByActivo(true)).thenReturn(List.of(entity));
        when(rutaDomicilioMapper.toDTO(entity)).thenReturn(dto);

        var result = rutaDomicilioService.findAllActive();

        assertEquals(1, result.size());
    }

    @Test
    void findById_WhenExists_ReturnsDTO() {
        when(rutaDomicilioRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(rutaDomicilioMapper.toDTO(entity)).thenReturn(dto);

        var result = rutaDomicilioService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Barrio Test", result.get().getBarrio());
    }

    @Test
    void findById_WhenNotExists_ReturnsEmpty() {
        when(rutaDomicilioRepository.findById(99L)).thenReturn(Optional.empty());

        var result = rutaDomicilioService.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByBarrioContaining_ReturnsMatching() {
        when(rutaDomicilioRepository.findByBarrioContainingIgnoreCase("Test")).thenReturn(List.of(entity));
        when(rutaDomicilioMapper.toDTO(entity)).thenReturn(dto);

        var result = rutaDomicilioService.findByBarrioContaining("Test");

        assertEquals(1, result.size());
    }

    @Test
    void save_ReturnsSavedDTO() {
        when(rutaDomicilioMapper.toEntity(dto)).thenReturn(entity);
        when(rutaDomicilioRepository.save(entity)).thenReturn(entity);
        when(rutaDomicilioMapper.toDTO(entity)).thenReturn(dto);

        var result = rutaDomicilioService.save(dto);

        assertNotNull(result);
        assertEquals("Barrio Test", result.getBarrio());
        verify(auditService).log(eq("CREAR_RUTA"), anyString());
    }

    @Test
    void update_WhenExists_UpdatesAndReturns() {
        when(rutaDomicilioRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(rutaDomicilioMapper.toEntity(any())).thenReturn(entity);
        when(rutaDomicilioRepository.save(entity)).thenReturn(entity);
        when(rutaDomicilioMapper.toDTO(entity)).thenReturn(dto);

        var result = rutaDomicilioService.update(dto);

        assertNotNull(result);
        verify(auditService).log(eq("EDITAR_RUTA"), anyString());
    }

    @Test
    void update_WhenNotExists_ThrowsException() {
        when(rutaDomicilioRepository.findById(99L)).thenReturn(Optional.empty());
        dto.setId(99L);

        assertThrows(RuntimeException.class, () -> rutaDomicilioService.update(dto));
    }

    @Test
    void delete_WhenExists_MarksInactive() {
        when(rutaDomicilioRepository.findById(1L)).thenReturn(Optional.of(entity));

        rutaDomicilioService.delete(1L);

        assertFalse(entity.getActivo());
        verify(rutaDomicilioRepository).save(entity);
        verify(auditService).log(eq("ELIMINAR_RUTA"), anyString());
    }

    @Test
    void delete_WhenNotExists_DoesNothing() {
        when(rutaDomicilioRepository.findById(99L)).thenReturn(Optional.empty());

        rutaDomicilioService.delete(99L);

        verify(rutaDomicilioRepository, never()).save(any());
    }
}
