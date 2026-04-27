package com.domicilio.domijose.services;

import com.domicilio.domijose.data.DataProviderMetodoPago;
import com.domicilio.domijose.dto.MetodoPagoDTO;
import com.domicilio.domijose.mappers.MetodoPagoMapper;
import com.domicilio.domijose.models.MetodoPago;
import com.domicilio.domijose.repositories.MetodoPagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetodoPagoServiceTest {

    @Mock
    private MetodoPagoRepository repository;

    @Mock
    private MetodoPagoMapper mapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private MetodoPagoService servicio;

    private MetodoPago testCuenta;
    private MetodoPago testQR;
    private MetodoPagoDTO testCuentaDTO;
    private MetodoPagoDTO testQRDTO;
    private MockMultipartFile qrFile;

    @BeforeEach
    void setUp() {
        testCuenta = DataProviderMetodoPago.createCuenta();
        testQR = DataProviderMetodoPago.createQR();
        testCuentaDTO = DataProviderMetodoPago.createCuentaDTO();
        testQRDTO = DataProviderMetodoPago.createQRDTO();
        qrFile = DataProviderMetodoPago.createQRFile();
    }

    @Test
    void getMetodosActivos_ShouldReturnActiveMethods() {
        // Arrange
        List<MetodoPago> metodos = List.of(testCuenta, testQR);
        when(repository.findByActivoTrue()).thenReturn(metodos);
        when(mapper.toDto(testCuenta)).thenReturn(testCuentaDTO);
        when(mapper.toDto(testQR)).thenReturn(testQRDTO);

        // Act
        List<MetodoPagoDTO> result = servicio.getMetodosActivos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).findByActivoTrue();
    }

    @Test
    void getCuentas_ShouldReturnOnlyBankAccounts() {
        // Arrange
        List<MetodoPago> metodos = List.of(testCuenta, testQR);
        when(repository.findByActivoTrue()).thenReturn(metodos);

        // Act
        List<MetodoPagoDTO> result = servicio.getCuentas();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Banco de Ejemplo", result.get(0).getBanco());
        verify(repository).findByActivoTrue();
    }

    @Test
    void getQrs_ShouldReturnOnlyQRs() {
        // Arrange
        List<MetodoPago> metodos = List.of(testCuenta, testQR);
        when(repository.findByActivoTrue()).thenReturn(metodos);

        // Act
        List<MetodoPagoDTO> result = servicio.getQrs();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getQrUrl());
        assertNull(result.get(0).getBanco());
        verify(repository).findByActivoTrue();
    }

    @Test
    void saveCuenta_WithValidData_ShouldSaveAccount() {
        // Arrange
        when(repository.save(any(MetodoPago.class))).thenReturn(testCuenta);
        when(mapper.toEntity(testCuentaDTO)).thenReturn(testCuenta);
        when(mapper.toDto(testCuenta)).thenReturn(testCuentaDTO);

        // Act
        MetodoPagoDTO result = servicio.saveCuenta(testCuentaDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Banco de Ejemplo", result.getBanco());
        assertNull(result.getQrUrl());
        assertTrue(result.isActivo());
        verify(repository).save(any(MetodoPago.class));
    }

    @Test
    void saveQr_WithValidData_ShouldSaveQR() {
        // Arrange
        testCuentaDTO.setBanco(null);
        testCuentaDTO.setTipoCuenta(null);
        testCuentaDTO.setNumeroCuenta(null);
        testCuentaDTO.setNombreTitular(null);
        testCuentaDTO.setQrFile(qrFile);
        
        when(fileService.saveImage(eq(qrFile), eq(true))).thenReturn("images/qr/qr-123.png");
        when(repository.save(any(MetodoPago.class))).thenReturn(testQR);
        when(mapper.toEntity(any(MetodoPagoDTO.class))).thenReturn(testQR);
        when(mapper.toDto(testQR)).thenReturn(testQRDTO);

        // Act
        MetodoPagoDTO result = servicio.saveQr(testCuentaDTO);

        // Assert
        assertNotNull(result);
        assertEquals("images/qr/qr-123.png", result.getQrUrl());
        assertNull(result.getBanco());
        assertTrue(result.isActivo());
        verify(fileService).saveImage(qrFile, true);
        verify(repository).save(any(MetodoPago.class));
    }

    @Test
    void saveQr_WithoutFile_ShouldSaveQRWithoutUrl() {
        // Arrange
        testCuentaDTO.setQrFile(null);
        
        when(repository.save(any(MetodoPago.class))).thenReturn(testQR);
        when(mapper.toEntity(any(MetodoPagoDTO.class))).thenReturn(testQR);
        when(mapper.toDto(testQR)).thenReturn(testQRDTO);

        // Act
        MetodoPagoDTO result = servicio.saveQr(testCuentaDTO);

        // Assert
        assertNotNull(result);
        assertNull(result.getQrUrl());
        verify(fileService, never()).saveImage(any(), anyBoolean());
        verify(repository).save(any(MetodoPago.class));
    }

    @Test
    void delete_WithValidId_ShouldDeleteMethod() {
        // Arrange
        doNothing().when(repository).deleteById(anyLong());

        // Act
        servicio.delete(testCuenta.getId());

        // Assert
        verify(repository).deleteById(testCuenta.getId());
    }
}
