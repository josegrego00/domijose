package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.DashboardUpdateEvent;
import com.josepinodev.appdomirest.dto.carrito.CarritoDTO;
import com.josepinodev.appdomirest.dto.carrito.ItemCarritoDTO;
import com.josepinodev.appdomirest.dto.venta.DetalleVentaDTO;
import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import com.josepinodev.appdomirest.mapper.DetalleVentaMapper;
import com.josepinodev.appdomirest.mapper.VentaMapper;
import com.josepinodev.appdomirest.model.*;
import com.josepinodev.appdomirest.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private DetalleVentaRepository detalleVentaRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private RutaDomicilioRepository rutaDomicilioRepository;
    @Mock private CuentaBancoRepository cuentaBancoRepository;
    @Mock private VentaMapper ventaMapper;
    @Mock private DetalleVentaMapper detalleVentaMapper;
    @Mock private NotificacionWebSocketService notificacionWebSocketService;
    @Mock private AuditService auditService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private VentaService ventaService;

    private UserEntity usuario;
    private ProductoEntity producto;
    private RutaDomicilioEntity ruta;
    private CuentaBancoEntity cuentaBanco;
    private VentaEntity venta;
    private VentaDTO ventaDTO;
    private CarritoDTO carrito;
    private ItemCarritoDTO item;
    private DetalleVentaEntity detalle;
    private DetalleVentaDTO detalleDTO;

    @BeforeEach
    void setUp() {
        usuario = new UserEntity();
        usuario.setId(1L);
        usuario.setUsername("testuser");
        usuario.setTelefono("3001234567");

        producto = new ProductoEntity();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecioVenta(new BigDecimal("10000"));

        ruta = new RutaDomicilioEntity();
        ruta.setId(1L);
        ruta.setBarrio("Barrio Test");
        ruta.setPrecio(new BigDecimal("5000"));

        cuentaBanco = new CuentaBancoEntity();
        cuentaBanco.setId(1L);
        cuentaBanco.setBanco("Banco Test");

        item = new ItemCarritoDTO(1L, "Producto Test", new BigDecimal("10000"), null, BigDecimal.ONE);
        carrito = new CarritoDTO();
        carrito.setItems(new ArrayList<>(List.of(item)));
        carrito.recalcularTotal();

        detalle = new DetalleVentaEntity();
        detalle.setId(1L);
        detalle.setProducto(producto);
        detalle.setCantidad(BigDecimal.ONE);
        detalle.setPrecioUnitario(new BigDecimal("10000"));
        detalle.setSubtotal(new BigDecimal("10000"));

        detalleDTO = new DetalleVentaDTO();
        detalleDTO.setProductoId(1L);
        detalleDTO.setProductoNombre("Producto Test");
        detalleDTO.setCantidad(BigDecimal.ONE);
        detalleDTO.setPrecioUnitario(new BigDecimal("10000"));
        detalleDTO.setSubtotal(new BigDecimal("10000"));

        venta = new VentaEntity();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setEstado(EVentaEstado.PENDIENTE);
        venta.setTotal(new BigDecimal("10000"));
        venta.setActivo(true);
        venta.setEsDomicilio(false);
        venta.setDetalles(new ArrayList<>(List.of(detalle)));

        ventaDTO = new VentaDTO();
        ventaDTO.setId(1L);
        ventaDTO.setUsuarioId(1L);
        ventaDTO.setUsuarioUsername("testuser");
        ventaDTO.setEstado(EVentaEstado.PENDIENTE);
        ventaDTO.setTotal(new BigDecimal("10000"));
        ventaDTO.setSubtotal(new BigDecimal("10000"));
        ventaDTO.setEsDomicilio(false);
    }

    // ---- crearVenta ----

    @Test
    void crearVenta_SinDomicilio_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(VentaEntity.class))).thenReturn(venta);
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        VentaDTO result = ventaService.crearVenta(carrito, 1L, null, null, false, "EFECTIVO", new BigDecimal("10000"), null);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(notificacionWebSocketService).notificarNuevaVenta(any(VentaDTO.class));
        verify(auditService).log(eq("CREAR_PEDIDO"), anyString());
    }

    @Test
    void crearVenta_ConDomicilio_Success() {
        venta.setEsDomicilio(true);
        venta.setCostoDomicilio(new BigDecimal("5000"));
        venta.setTotal(new BigDecimal("15000"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(rutaDomicilioRepository.findById(1L)).thenReturn(Optional.of(ruta));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(VentaEntity.class))).thenReturn(venta);
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        VentaDTO result = ventaService.crearVenta(carrito, 1L, "Calle 123", 1L, true, "EFECTIVO", new BigDecimal("15000"), null);

        assertNotNull(result);
        verify(rutaDomicilioRepository).findById(1L);
    }

    @Test
    void crearVenta_ConDomicilioSinRuta_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(RuntimeException.class,
                () -> ventaService.crearVenta(carrito, 1L, "Calle 123", null, true, null, null, null));
    }

    @Test
    void crearVenta_ConDomicilioSinDireccion_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(RuntimeException.class,
                () -> ventaService.crearVenta(carrito, 1L, "", 1L, true, null, null, null));
    }

    @Test
    void crearVenta_PagoEfectivo_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(VentaEntity.class))).thenReturn(venta);
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        VentaDTO result = ventaService.crearVenta(carrito, 1L, null, null, false, "EFECTIVO", new BigDecimal("10000"), null);

        assertNotNull(result);
    }

    @Test
    void crearVenta_PagoEfectivoInsuficiente_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(RuntimeException.class,
                () -> ventaService.crearVenta(carrito, 1L, null, null, false, "EFECTIVO", new BigDecimal("5000"), null));
    }

    @Test
    void crearVenta_PagoTransferencia_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(cuentaBancoRepository.findById(1L)).thenReturn(Optional.of(cuentaBanco));
        when(ventaRepository.save(any(VentaEntity.class))).thenReturn(venta);
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        VentaDTO result = ventaService.crearVenta(carrito, 1L, null, null, false, "TRANSFERENCIA", null, 1L);

        assertNotNull(result);
    }

    @Test
    void crearVenta_PagoTransferenciaSinBanco_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(RuntimeException.class,
                () -> ventaService.crearVenta(carrito, 1L, null, null, false, "TRANSFERENCIA", null, null));
    }

    @Test
    void crearVenta_PagoMixto_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(cuentaBancoRepository.findById(1L)).thenReturn(Optional.of(cuentaBanco));
        when(ventaRepository.save(any(VentaEntity.class))).thenReturn(venta);
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        VentaDTO result = ventaService.crearVenta(carrito, 1L, null, null, false, "MIXTO", new BigDecimal("3000"), 1L);

        assertNotNull(result);
    }

    @Test
    void crearVenta_PagoMixtoEfectivoMayorOIgualTotal_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(RuntimeException.class,
                () -> ventaService.crearVenta(carrito, 1L, null, null, false, "MIXTO", new BigDecimal("10000"), 1L));
    }

    @Test
    void crearVenta_SinMetodoPago_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(RuntimeException.class,
                () -> ventaService.crearVenta(carrito, 1L, null, null, false, null, null, null));
    }

    @Test
    void crearVenta_UsuarioNoEncontrado_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> ventaService.crearVenta(carrito, 99L, null, null, false, null, null, null));
    }

    @Test
    void crearVenta_ProductoNoEncontrado_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> ventaService.crearVenta(carrito, 1L, null, null, false, null, null, null));
    }

    // ---- getVentasUsuario ----

    @Test
    void getVentasUsuario_ConFiltroHoy_ReturnsFiltered() {
        Page<VentaEntity> page = new PageImpl<>(List.of(venta));
        when(ventaRepository.findByUsuarioIdAndActivoTrueAndFechaBetween(anyLong(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        Page<VentaDTO> result = ventaService.getVentasUsuario(1L, PageRequest.of(0, 10), "hoy");

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getVentasUsuario_ConFiltroTodos_ReturnsAll() {
        Page<VentaEntity> page = new PageImpl<>(List.of(venta));
        when(ventaRepository.findByUsuarioIdAndActivoTrue(anyLong(), any(Pageable.class))).thenReturn(page);
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        Page<VentaDTO> result = ventaService.getVentasUsuario(1L, PageRequest.of(0, 10), "todos");

        assertEquals(1, result.getContent().size());
    }

    // ---- getAllVentas ----

    @Test
    void getAllVentas_ReturnsPage() {
        Page<VentaEntity> page = new PageImpl<>(List.of(venta));
        when(ventaRepository.findByActivoTrue(any(Pageable.class))).thenReturn(page);
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        Page<VentaDTO> result = ventaService.getAllVentas(PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    // ---- getVentasByEstado ----

    @Test
    void getVentasByEstado_ReturnsFiltered() {
        Page<VentaEntity> page = new PageImpl<>(List.of(venta));
        when(ventaRepository.findByEstadoAndActivoTrue(EVentaEstado.PENDIENTE, PageRequest.of(0, 10))).thenReturn(page);
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        Page<VentaDTO> result = ventaService.getVentasByEstado(EVentaEstado.PENDIENTE, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    // ---- getVentaById ----

    @Test
    void getVentaById_WhenExists_ReturnsDTO() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaMapper.toDTO(venta)).thenReturn(ventaDTO);
        when(detalleVentaMapper.toDTO(detalle)).thenReturn(detalleDTO);

        VentaDTO result = ventaService.getVentaById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getVentaById_WhenNotExists_ThrowsException() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> ventaService.getVentaById(99L));
    }

    // ---- tomarPedido ----

    @Test
    void tomarPedido_WhenPendiente_Success() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        VentaEntity saved = new VentaEntity();
        saved.setId(1L);
        saved.setEstado(EVentaEstado.PEDIDO_TOMADO);
        saved.setUsuario(usuario);
        when(ventaRepository.save(venta)).thenReturn(saved);
        VentaDTO takenDTO = new VentaDTO();
        takenDTO.setId(1L);
        takenDTO.setEstado(EVentaEstado.PEDIDO_TOMADO);
        when(ventaMapper.toDTO(saved)).thenReturn(takenDTO);

        VentaDTO result = ventaService.tomarPedido(1L);

        assertEquals(EVentaEstado.PEDIDO_TOMADO, result.getEstado());
        verify(notificacionWebSocketService).notificarPedidoTomado(any(VentaDTO.class), eq("testuser"));
        verify(auditService).log(eq("TOMAR_PEDIDO"), anyString());
    }

    @Test
    void tomarPedido_WhenNotPendiente_ThrowsException() {
        venta.setEstado(EVentaEstado.COMPLETADO);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThrows(RuntimeException.class, () -> ventaService.tomarPedido(1L));
    }

    // ---- enviarPedido ----

    @Test
    void enviarPedido_WhenTomado_Success() {
        venta.setEstado(EVentaEstado.PEDIDO_TOMADO);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        VentaEntity saved = new VentaEntity();
        saved.setId(1L);
        saved.setEstado(EVentaEstado.ENVIADO);
        saved.setUsuario(usuario);
        when(ventaRepository.save(venta)).thenReturn(saved);
        VentaDTO sentDTO = new VentaDTO();
        sentDTO.setId(1L);
        sentDTO.setEstado(EVentaEstado.ENVIADO);
        when(ventaMapper.toDTO(saved)).thenReturn(sentDTO);

        VentaDTO result = ventaService.enviarPedido(1L);

        assertEquals(EVentaEstado.ENVIADO, result.getEstado());
        verify(notificacionWebSocketService).notificarPedidoEnviado(any(VentaDTO.class), eq("testuser"));
    }

    @Test
    void enviarPedido_WhenNotTomado_ThrowsException() {
        venta.setEstado(EVentaEstado.PENDIENTE);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThrows(RuntimeException.class, () -> ventaService.enviarPedido(1L));
    }

    // ---- completarVenta ----

    @Test
    void completarVenta_WhenEnviado_Success() {
        venta.setEstado(EVentaEstado.ENVIADO);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        VentaEntity saved = new VentaEntity();
        saved.setId(1L);
        saved.setEstado(EVentaEstado.COMPLETADO);
        saved.setUsuario(usuario);
        when(ventaRepository.save(venta)).thenReturn(saved);
        VentaDTO completedDTO = new VentaDTO();
        completedDTO.setId(1L);
        completedDTO.setEstado(EVentaEstado.COMPLETADO);
        when(ventaMapper.toDTO(saved)).thenReturn(completedDTO);

        VentaDTO result = ventaService.completarVenta(1L);

        assertEquals(EVentaEstado.COMPLETADO, result.getEstado());
        verify(notificacionWebSocketService).notificarPedidoCompletado(any(VentaDTO.class), eq("testuser"));
    }

    @Test
    void completarVenta_WhenNotEnviado_ThrowsException() {
        venta.setEstado(EVentaEstado.PENDIENTE);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThrows(RuntimeException.class, () -> ventaService.completarVenta(1L));
    }

    // ---- cancelarVenta (admin) ----

    @Test
    void cancelarVenta_WhenPendiente_Success() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        VentaEntity saved = new VentaEntity();
        saved.setId(1L);
        saved.setEstado(EVentaEstado.CANCELADA);
        saved.setUsuario(usuario);
        when(ventaRepository.save(venta)).thenReturn(saved);
        VentaDTO cancelledDTO = new VentaDTO();
        cancelledDTO.setId(1L);
        cancelledDTO.setEstado(EVentaEstado.CANCELADA);
        when(ventaMapper.toDTO(saved)).thenReturn(cancelledDTO);

        VentaDTO result = ventaService.cancelarVenta(1L);

        assertEquals(EVentaEstado.CANCELADA, result.getEstado());
        verify(notificacionWebSocketService).notificarPedidoCancelado(any(VentaDTO.class), eq("testuser"));
    }

    @Test
    void cancelarVenta_WhenEstadoInvalido_ThrowsException() {
        venta.setEstado(EVentaEstado.COMPLETADO);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThrows(RuntimeException.class, () -> ventaService.cancelarVenta(1L));
    }

    // ---- cancelarVentaCliente ----

    @Test
    void cancelarVentaCliente_WhenPendienteAndOwner_Success() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        VentaEntity saved = new VentaEntity();
        saved.setId(1L);
        saved.setEstado(EVentaEstado.CANCELADA);
        saved.setUsuario(usuario);
        when(ventaRepository.save(venta)).thenReturn(saved);
        VentaDTO cancelledDTO = new VentaDTO();
        cancelledDTO.setId(1L);
        cancelledDTO.setEstado(EVentaEstado.CANCELADA);
        when(ventaMapper.toDTO(saved)).thenReturn(cancelledDTO);

        VentaDTO result = ventaService.cancelarVentaCliente(1L, 1L);

        assertEquals(EVentaEstado.CANCELADA, result.getEstado());
        verify(notificacionWebSocketService).notificarCancelacionCliente(any(VentaDTO.class), eq("testuser"));
    }

    @Test
    void cancelarVentaCliente_WhenNotOwner_ThrowsException() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThrows(RuntimeException.class, () -> ventaService.cancelarVentaCliente(1L, 99L));
    }

    @Test
    void cancelarVentaCliente_WhenNotPendiente_ThrowsException() {
        venta.setEstado(EVentaEstado.PEDIDO_TOMADO);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThrows(RuntimeException.class, () -> ventaService.cancelarVentaCliente(1L, 1L));
    }

    // ---- countPendientes ----

    @Test
    void countPendientes_ReturnsCount() {
        when(ventaRepository.countByEstadoAndActivoTrue(EVentaEstado.PENDIENTE)).thenReturn(3L);

        assertEquals(3L, ventaService.countPendientes());
    }
}
