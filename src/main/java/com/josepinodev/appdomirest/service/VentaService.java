package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.DashboardUpdateEvent;
import com.josepinodev.appdomirest.dto.carrito.CarritoDTO;
import com.josepinodev.appdomirest.dto.venta.DetalleVentaDTO;
import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import com.josepinodev.appdomirest.mapper.DetalleVentaMapper;
import com.josepinodev.appdomirest.mapper.VentaMapper;
import com.josepinodev.appdomirest.model.CuentaBancoEntity;
import com.josepinodev.appdomirest.model.DetalleVentaEntity;
import com.josepinodev.appdomirest.model.EMetodoPago;
import com.josepinodev.appdomirest.model.EVentaEstado;
import com.josepinodev.appdomirest.model.ProductoEntity;
import com.josepinodev.appdomirest.model.RutaDomicilioEntity;
import com.josepinodev.appdomirest.model.UserEntity;
import com.josepinodev.appdomirest.model.VentaEntity;
import com.josepinodev.appdomirest.repository.CuentaBancoRepository;
import com.josepinodev.appdomirest.repository.DetalleVentaRepository;
import com.josepinodev.appdomirest.repository.ProductoRepository;
import com.josepinodev.appdomirest.repository.RutaDomicilioRepository;
import com.josepinodev.appdomirest.repository.UserRepository;
import com.josepinodev.appdomirest.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final UserRepository userRepository;
    private final ProductoRepository productoRepository;
    private final RutaDomicilioRepository rutaDomicilioRepository;
    private final CuentaBancoRepository cuentaBancoRepository;
    private final VentaMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final NotificacionWebSocketService notificacionWebSocketService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public VentaDTO crearVenta(CarritoDTO carrito, Long usuarioId, String direccion, Long rutaId, Boolean esDomicilio,
                                String metodoPago, BigDecimal efectivoRecibido, Long cuentaBancoId) {
        log.info("Creando venta para usuario: {}, items: {}, esDomicilio: {}, metodoPago: {}", usuarioId, carrito.getItems().size(), esDomicilio, metodoPago);

        UserEntity usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        VentaEntity venta = new VentaEntity();
        venta.setUsuario(usuario);
        venta.setEstado(EVentaEstado.PENDIENTE);
        venta.setActivo(true);
        venta.setEsDomicilio(esDomicilio);

        BigDecimal subtotal = BigDecimal.ZERO;

        if (Boolean.TRUE.equals(esDomicilio)) {
            if (rutaId == null || rutaId <= 0) {
                throw new RuntimeException("Selecciona un barrio para el domicilio");
            }
            if (direccion == null || direccion.trim().isEmpty()) {
                throw new RuntimeException("Escribe la dirección de entrega");
            }
            venta.setDireccion(direccion.trim());
            RutaDomicilioEntity ruta = rutaDomicilioRepository.findById(rutaId)
                    .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));
            venta.setRutaDomicilio(ruta);
            venta.setCostoDomicilio(ruta.getPrecio());
            subtotal = subtotal.add(ruta.getPrecio());
        }

        for (var item : carrito.getItems()) {
            ProductoEntity producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            DetalleVentaEntity detalle = new DetalleVentaEntity();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecio());
            detalle.setSubtotal(item.getSubtotal());
            detalle.setObservacion(item.getObservacion());

            venta.getDetalles().add(detalle);
            subtotal = subtotal.add(item.getSubtotal());
        }

        venta.setTotal(subtotal);

        if (metodoPago != null) {
            venta.setMetodoPago(EMetodoPago.valueOf(metodoPago));
            switch (metodoPago) {
                case "EFECTIVO" -> {
                    if (efectivoRecibido == null || efectivoRecibido.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new RuntimeException("Indica con cuánto dinero vas a pagar");
                    }
                    if (efectivoRecibido.compareTo(subtotal) < 0) {
                        throw new RuntimeException("El efectivo recibido ($" + efectivoRecibido + ") es menor que el total ($" + subtotal + ")");
                    }
                    venta.setEfectivoRecibido(efectivoRecibido);
                    venta.setEfectivoCambio(efectivoRecibido.subtract(subtotal));
                }
                case "TRANSFERENCIA" -> {
                    if (cuentaBancoId == null) {
                        throw new RuntimeException("Selecciona un banco para la transferencia");
                    }
                    venta.setMontoTransferencia(subtotal);
                    CuentaBancoEntity cuenta = cuentaBancoRepository.findById(cuentaBancoId)
                            .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));
                    venta.setCuentaBanco(cuenta);
                }
                case "MIXTO" -> {
                    if (efectivoRecibido == null || efectivoRecibido.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new RuntimeException("Indica cuánto vas a pagar en efectivo");
                    }
                    if (efectivoRecibido.compareTo(subtotal) >= 0) {
                        throw new RuntimeException("Si pagas todo en efectivo, selecciona el método 'Efectivo'");
                    }
                    if (cuentaBancoId == null) {
                        throw new RuntimeException("Selecciona un banco para transferir el resto");
                    }
                    venta.setEfectivoRecibido(efectivoRecibido);
                    CuentaBancoEntity cuenta = cuentaBancoRepository.findById(cuentaBancoId)
                            .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));
                    venta.setCuentaBanco(cuenta);
                    venta.setMontoTransferencia(subtotal.subtract(efectivoRecibido));
                    venta.setEfectivoCambio(BigDecimal.ZERO);
                }
            }
        } else {
            throw new RuntimeException("Selecciona un método de pago");
        }

        VentaEntity savedVenta = ventaRepository.save(venta);

        log.info("Venta creada con ID: {}, total: {}, metodoPago: {}", savedVenta.getId(), savedVenta.getTotal(), savedVenta.getMetodoPago());

        VentaDTO dto = ventaMapper.toDTO(savedVenta);

        notificacionWebSocketService.notificarNuevaVenta(dto);
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("CREAR_PEDIDO", "Pedido #" + savedVenta.getId() + " creado por " + usuario.getUsername() + " - Total: $" + savedVenta.getTotal() + " - Pago: " + metodoPago);
        dto.setSubtotal(carrito.getTotal());
        dto.setDetalles(savedVenta.getDetalles().stream()
                .map(detalleVentaMapper::toDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    public Page<VentaDTO> getVentasUsuario(Long usuarioId, Pageable pageable, String filtro) {
        Page<VentaEntity> page;
        if ("hoy".equals(filtro)) {
            LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusDays(1);
            page = ventaRepository.findByUsuarioIdAndActivoTrueAndFechaBetween(usuarioId, start, end, pageable);
        } else {
            page = ventaRepository.findByUsuarioIdAndActivoTrue(usuarioId, pageable);
        }
        return page.map(this::toVentaDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<VentaDTO> getAllVentas(Pageable pageable) {
        return ventaRepository.findByActivoTrue(pageable).map(this::toVentaDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<VentaDTO> getVentasByEstado(EVentaEstado estado, Pageable pageable) {
        return ventaRepository.findByEstadoAndActivoTrue(estado, pageable).map(this::toVentaDTO);
    }

    private VentaDTO toVentaDTO(VentaEntity venta) {
        VentaDTO dto = ventaMapper.toDTO(venta);
        dto.setSubtotal(venta.getTotal().subtract(
                venta.getCostoDomicilio() != null ? venta.getCostoDomicilio() : BigDecimal.ZERO));
        dto.setDetalles(venta.getDetalles().stream()
                .map(detalleVentaMapper::toDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    public VentaDTO getVentaById(Long id) {
        VentaEntity venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        return toVentaDTO(venta);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public VentaDTO tomarPedido(Long id) {
        VentaEntity venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (venta.getEstado() != EVentaEstado.PENDIENTE) {
            throw new RuntimeException("Solo se pueden tomar pedidos pendientes");
        }

        venta.setEstado(EVentaEstado.PEDIDO_TOMADO);
        VentaEntity saved = ventaRepository.save(venta);

        log.info("Pedido {} tomado en preparacion", id);

        VentaDTO dto = ventaMapper.toDTO(saved);
        notificacionWebSocketService.notificarPedidoTomado(dto, saved.getUsuario().getUsername());
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("TOMAR_PEDIDO", "Pedido #" + id + " tomado en preparacion por el admin");
        return dto;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public VentaDTO enviarPedido(Long id) {
        VentaEntity venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (venta.getEstado() != EVentaEstado.PEDIDO_TOMADO) {
            throw new RuntimeException("Solo se pueden enviar pedidos en preparacion");
        }

        venta.setEstado(EVentaEstado.ENVIADO);
        VentaEntity saved = ventaRepository.save(venta);

        log.info("Pedido {} enviado a domicilio", id);

        VentaDTO dto = ventaMapper.toDTO(saved);
        notificacionWebSocketService.notificarPedidoEnviado(dto, saved.getUsuario().getUsername());
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("ENVIAR_PEDIDO", "Pedido #" + id + " enviado a domicilio por el admin");
        return dto;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public VentaDTO completarVenta(Long id) {
        VentaEntity venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (venta.getEstado() != EVentaEstado.ENVIADO) {
            throw new RuntimeException("Solo se pueden completar pedidos enviados");
        }

        venta.setEstado(EVentaEstado.COMPLETADO);
        VentaEntity saved = ventaRepository.save(venta);

        log.info("Pedido {} completado", id);
        VentaDTO dto = ventaMapper.toDTO(saved);
        notificacionWebSocketService.notificarPedidoCompletado(dto, saved.getUsuario().getUsername());
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("COMPLETAR_PEDIDO", "Pedido #" + id + " completado por el admin");
        return dto;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public VentaDTO cancelarVenta(Long id) {
        VentaEntity venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (venta.getEstado() != EVentaEstado.PENDIENTE && venta.getEstado() != EVentaEstado.PEDIDO_TOMADO) {
            throw new RuntimeException("Solo se pueden cancelar pedidos pendientes o en preparacion");
        }

        venta.setEstado(EVentaEstado.CANCELADA);
        VentaEntity saved = ventaRepository.save(venta);

        log.info("Pedido {} cancelado", id);
        VentaDTO dto = ventaMapper.toDTO(saved);
        notificacionWebSocketService.notificarPedidoCancelado(dto, saved.getUsuario().getUsername());
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("CANCELAR_PEDIDO", "Pedido #" + id + " cancelado por el admin");
        return dto;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public VentaDTO cancelarVentaCliente(Long id, Long usuarioId) {
        VentaEntity venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (!venta.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("Este pedido no te pertenece");
        }

        if (venta.getEstado() != EVentaEstado.PENDIENTE) {
            throw new RuntimeException("Solo puedes cancelar pedidos pendientes antes de que el admin los tome");
        }

        venta.setEstado(EVentaEstado.CANCELADA);
        VentaEntity saved = ventaRepository.save(venta);

        log.info("Pedido {} cancelado por el cliente", id);
        VentaDTO dto = ventaMapper.toDTO(saved);
        notificacionWebSocketService.notificarPedidoCancelado(dto, saved.getUsuario().getUsername());
        notificacionWebSocketService.notificarCancelacionCliente(dto, saved.getUsuario().getUsername());
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("CANCELAR_PEDIDO_CLIENTE", "Pedido #" + id + " cancelado por el cliente " + saved.getUsuario().getUsername());
        return dto;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public long countPendientes() {
        return ventaRepository.countByEstadoAndActivoTrue(EVentaEstado.PENDIENTE);
    }
}