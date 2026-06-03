package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.DashboardUpdateEvent;
import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import com.josepinodev.appdomirest.mapper.ProductoMapper;
import com.josepinodev.appdomirest.model.ProductoEntity;
import com.josepinodev.appdomirest.model.UserEntity;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.repository.ProductoRepository;
import com.josepinodev.appdomirest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductoMapper productoMapper;
    @Mock private AuditService auditService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ProductoService productoService;

    private ProductoEntity entity;
    private ProductoDTO dto;
    private UserEntity creador;

    @BeforeEach
    void setUp() {
        creador = new UserEntity();
        creador.setId(1L);
        creador.setUsername("admin");

        entity = new ProductoEntity();
        entity.setId(1L);
        entity.setNombre("Test Producto");
        entity.setPrecioVenta(new BigDecimal("10000"));
        entity.setActivo(true);
        entity.setDisponible(true);
        entity.setUsuarioCreador(creador);
        entity.setCodigo("PROD-0001");

        dto = new ProductoDTO();
        dto.setId(1L);
        dto.setNombre("Test Producto");
        dto.setPrecioVenta(new BigDecimal("10000"));
        dto.setActivo(true);
        dto.setDisponible(true);
        dto.setCodigo("PROD-0001");
    }

    @Test
    void findAll_ReturnsAllProducts() {
        when(productoRepository.findAll()).thenReturn(List.of(entity));
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        List<ProductoDTO> result = productoService.findAll();

        assertEquals(1, result.size());
        assertEquals("Test Producto", result.get(0).getNombre());
        verify(productoRepository).findAll();
    }

    @Test
    void findAllPaginated_ReturnsPage() {
        Page<ProductoEntity> page = new PageImpl<>(List.of(entity));
        when(productoRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        Page<ProductoDTO> result = productoService.findAllPaginated(0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("Test Producto", result.getContent().get(0).getNombre());
        verify(productoRepository, times(2)).findAll(any(PageRequest.class));
    }

    @Test
    void findById_WhenExists_ReturnsDTO() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        Optional<ProductoDTO> result = productoService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Producto", result.get().getNombre());
    }

    @Test
    void findById_WhenNotExists_ReturnsEmpty() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ProductoDTO> result = productoService.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCodigo_WhenExists_ReturnsDTO() {
        when(productoRepository.findByCodigo("PROD-0001")).thenReturn(Optional.of(entity));
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        Optional<ProductoDTO> result = productoService.findByCodigo("PROD-0001");

        assertTrue(result.isPresent());
        assertEquals("PROD-0001", result.get().getCodigo());
    }

    @Test
    void findByCodigo_WhenNotExists_ReturnsEmpty() {
        when(productoRepository.findByCodigo("INVALID")).thenReturn(Optional.empty());

        Optional<ProductoDTO> result = productoService.findByCodigo("INVALID");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByActivo_ReturnsFilteredList() {
        when(productoRepository.findByActivo(true)).thenReturn(List.of(entity));
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        List<ProductoDTO> result = productoService.findByActivo(true);

        assertEquals(1, result.size());
        verify(productoRepository).findByActivo(true);
    }

    @Test
    void findByNombreContaining_ReturnsMatching() {
        when(productoRepository.findByNombreContainingIgnoreCase("Test")).thenReturn(List.of(entity));
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        List<ProductoDTO> result = productoService.findByNombreContaining("Test");

        assertEquals(1, result.size());
        verify(productoRepository).findByNombreContainingIgnoreCase("Test");
    }

    @Test
    void save_WithNullCode_GeneratesCodeAndSaves() {
        dto.setCodigo(null);
        ProductoEntity entityWithoutId = new ProductoEntity();
        entityWithoutId.setNombre("Test Producto");
        entityWithoutId.setCodigo("PROD-0002");

        when(productoRepository.findTopByOrderByCodigoDesc()).thenReturn(Optional.of(entity));
        when(productoMapper.toEntity(dto)).thenReturn(entityWithoutId);
        when(productoRepository.save(any(ProductoEntity.class))).thenReturn(entity);
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        try (MockedStatic<SecurityContextHolder> security = mockStatic(SecurityContextHolder.class);
             MockedStatic<SecurityContext> ctx = mockStatic(SecurityContext.class)) {
            Authentication auth = mock(Authentication.class);
            UserDetailsImpl userDetails = new UserDetailsImpl(1L, "admin", "pass", List.of(), true);
            when(auth.getPrincipal()).thenReturn(userDetails);
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(auth);
            security.when(SecurityContextHolder::getContext).thenReturn(context);
            when(userRepository.findById(1L)).thenReturn(Optional.of(creador));

            ProductoDTO result = productoService.save(dto);

            assertNotNull(result);
            verify(productoRepository).save(any(ProductoEntity.class));
            verify(auditService).log(eq("CREAR_PRODUCTO"), anyString());
            verify(eventPublisher).publishEvent(any(DashboardUpdateEvent.class));
        }
    }

    @Test
    void save_WithExistingCode_DoesNotRegenerate() {
        ProductoEntity entityWithoutId = new ProductoEntity();
        entityWithoutId.setNombre("Test Producto");

        when(productoMapper.toEntity(dto)).thenReturn(entityWithoutId);
        when(productoRepository.save(any(ProductoEntity.class))).thenReturn(entity);
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        try (MockedStatic<SecurityContextHolder> security = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            UserDetailsImpl userDetails = new UserDetailsImpl(1L, "admin", "pass", List.of(), true);
            when(auth.getPrincipal()).thenReturn(userDetails);
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(auth);
            security.when(SecurityContextHolder::getContext).thenReturn(context);
            when(userRepository.findById(1L)).thenReturn(Optional.of(creador));

            productoService.save(dto);

            verify(productoRepository, never()).findTopByOrderByCodigoDesc();
        }
    }

    @Test
    void save_WhenSecurityContextFails_SavesWithoutCreator() {
        when(productoMapper.toEntity(dto)).thenReturn(new ProductoEntity());
        when(productoRepository.save(any(ProductoEntity.class))).thenReturn(entity);
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        try (MockedStatic<SecurityContextHolder> security = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenThrow(new RuntimeException("No auth"));
            security.when(SecurityContextHolder::getContext).thenReturn(context);

            ProductoDTO result = productoService.save(dto);

            assertNotNull(result);
            verify(productoRepository).save(any(ProductoEntity.class));
        }
    }

    @Test
    void update_WhenExists_UpdatesAndReturns() {
        ProductoDTO updateDTO = new ProductoDTO();
        updateDTO.setId(1L);
        updateDTO.setNombre("Updated Producto");
        updateDTO.setCodigo("PROD-0001");
        updateDTO.setActivo(true);
        updateDTO.setPrecioVenta(new BigDecimal("15000"));

        ProductoEntity existingEntity = new ProductoEntity();
        existingEntity.setId(1L);
        existingEntity.setNombre("Old Name");
        existingEntity.setCodigo("PROD-0001");
        existingEntity.setFechaCreacion(null);
        existingEntity.setUsuarioCreador(creador);

        ProductoEntity updatedEntity = new ProductoEntity();
        updatedEntity.setId(1L);
        updatedEntity.setNombre("Updated Producto");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(productoMapper.toEntity(any(ProductoDTO.class))).thenReturn(updatedEntity);
        when(productoRepository.save(any(ProductoEntity.class))).thenReturn(updatedEntity);
        when(productoMapper.toDTO(updatedEntity)).thenReturn(updateDTO);

        ProductoDTO result = productoService.update(updateDTO);

        assertEquals("Updated Producto", result.getNombre());
        verify(auditService).log(eq("EDITAR_PRODUCTO"), anyString());
    }

    @Test
    void update_WhenNotExists_ThrowsException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        ProductoDTO badDTO = new ProductoDTO();
        badDTO.setId(99L);

        assertThrows(RuntimeException.class, () -> productoService.update(badDTO));
    }

    @Test
    void delete_WhenExists_MarksInactive() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(entity));

        productoService.delete(1L);

        assertFalse(entity.getActivo());
        verify(productoRepository).save(entity);
        verify(auditService).log(eq("ELIMINAR_PRODUCTO"), anyString());
    }

    @Test
    void delete_WhenNotExists_DoesNothing() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        productoService.delete(99L);

        verify(productoRepository, never()).save(any());
        verify(auditService, never()).log(anyString(), anyString());
    }

    @Test
    void countActive_ReturnsCount() {
        when(productoRepository.countByActivo(true)).thenReturn(5L);

        Long result = productoService.countActive();

        assertEquals(5L, result);
    }

    @Test
    void findEnPromocion_ReturnsOnlyPromoted() {
        ProductoEntity promoted = new ProductoEntity();
        promoted.setEnPromocion(true);
        ProductoEntity notPromoted = new ProductoEntity();
        notPromoted.setEnPromocion(false);
        ProductoDTO promotedDTO = new ProductoDTO();
        promotedDTO.setEnPromocion(true);

        when(productoRepository.findAll()).thenReturn(List.of(promoted, notPromoted));
        when(productoMapper.toDTO(promoted)).thenReturn(promotedDTO);

        List<ProductoDTO> result = productoService.findEnPromocion();

        assertEquals(1, result.size());
    }

    @Test
    void findAllActive_ReturnsOnlyActive() {
        when(productoRepository.findByActivo(true)).thenReturn(List.of(entity));
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        List<ProductoDTO> result = productoService.findAllActive();

        assertEquals(1, result.size());
    }

    @Test
    void searchByNombre_ReturnsOnlyActiveMatching() {
        ProductoEntity inactive = new ProductoEntity();
        inactive.setNombre("Test Inactive");
        inactive.setActivo(false);

        when(productoRepository.findByNombreContainingIgnoreCase("Test")).thenReturn(List.of(entity, inactive));
        when(productoMapper.toDTO(entity)).thenReturn(dto);

        List<ProductoDTO> result = productoService.searchByNombre("Test");

        assertEquals(1, result.size());
    }
}
