package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.cuenta.CuentaBancoDTO;
import com.josepinodev.appdomirest.mapper.CuentaBancoMapper;
import com.josepinodev.appdomirest.model.CuentaBancoEntity;
import com.josepinodev.appdomirest.repository.CuentaBancoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaBancoServiceTest {

    @Mock private CuentaBancoRepository cuentaBancoRepository;
    @Mock private CuentaBancoMapper cuentaBancoMapper;
    @Mock private AuditService auditService;
    @InjectMocks private CuentaBancoService cuentaBancoService;

    private CuentaBancoEntity entity;
    private CuentaBancoDTO dto;

    @BeforeEach
    void setUp() {
        entity = new CuentaBancoEntity();
        entity.setId(1L);
        entity.setBanco("Banco Test");
        entity.setTipoCuenta("Ahorros");
        entity.setNumeroCuenta("123456");
        entity.setTitular("Test User");
        entity.setActivo(true);

        dto = new CuentaBancoDTO();
        dto.setId(1L);
        dto.setBanco("Banco Test");
        dto.setTipoCuenta("Ahorros");
        dto.setNumeroCuenta("123456");
        dto.setTitular("Test User");
        dto.setActivo(true);
    }

    @Test
    void findAll_ReturnsAll() {
        when(cuentaBancoRepository.findAll()).thenReturn(List.of(entity));
        when(cuentaBancoMapper.toDTO(entity)).thenReturn(dto);

        var result = cuentaBancoService.findAll();

        assertEquals(1, result.size());
        assertEquals("Banco Test", result.get(0).getBanco());
    }

    @Test
    void findAllActive_ReturnsActive() {
        when(cuentaBancoRepository.findByActivoTrue()).thenReturn(List.of(entity));
        when(cuentaBancoMapper.toDTO(entity)).thenReturn(dto);

        var result = cuentaBancoService.findAllActive();

        assertEquals(1, result.size());
    }

    @Test
    void findById_WhenExists_ReturnsDTO() {
        when(cuentaBancoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(cuentaBancoMapper.toDTO(entity)).thenReturn(dto);

        var result = cuentaBancoService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Banco Test", result.get().getBanco());
    }

    @Test
    void findById_WhenNotExists_ReturnsEmpty() {
        when(cuentaBancoRepository.findById(99L)).thenReturn(Optional.empty());

        var result = cuentaBancoService.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ReturnsSavedDTO() {
        when(cuentaBancoMapper.toEntity(dto)).thenReturn(entity);
        when(cuentaBancoRepository.save(entity)).thenReturn(entity);
        when(cuentaBancoMapper.toDTO(entity)).thenReturn(dto);

        var result = cuentaBancoService.save(dto);

        assertNotNull(result);
        assertEquals("Banco Test", result.getBanco());
        verify(auditService).log(eq("CREAR_CUENTA_BANCO"), anyString());
    }

    @Test
    void update_WhenExists_UpdatesAndReturns() {
        when(cuentaBancoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(cuentaBancoMapper.toEntity(any())).thenReturn(entity);
        when(cuentaBancoRepository.save(entity)).thenReturn(entity);
        when(cuentaBancoMapper.toDTO(entity)).thenReturn(dto);

        var result = cuentaBancoService.update(dto);

        assertNotNull(result);
        verify(auditService).log(eq("EDITAR_CUENTA_BANCO"), anyString());
    }

    @Test
    void update_WhenNotExists_ThrowsException() {
        when(cuentaBancoRepository.findById(99L)).thenReturn(Optional.empty());
        dto.setId(99L);

        assertThrows(RuntimeException.class, () -> cuentaBancoService.update(dto));
    }

    @Test
    void delete_WhenExists_MarksInactive() {
        when(cuentaBancoRepository.findById(1L)).thenReturn(Optional.of(entity));

        cuentaBancoService.delete(1L);

        assertFalse(entity.getActivo());
        verify(cuentaBancoRepository).save(entity);
        verify(auditService).log(eq("ELIMINAR_CUENTA_BANCO"), anyString());
    }

    @Test
    void delete_WhenNotExists_DoesNothing() {
        when(cuentaBancoRepository.findById(99L)).thenReturn(Optional.empty());

        cuentaBancoService.delete(99L);

        verify(cuentaBancoRepository, never()).save(any());
    }
}
