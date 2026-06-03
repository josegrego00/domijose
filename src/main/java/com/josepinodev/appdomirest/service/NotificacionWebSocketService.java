package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.NotificacionDTO;
import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    private NotificacionDTO buildNotificacion(String tipo, String mensaje, VentaDTO ventaDTO, String estado) {
        BigDecimal total = ventaDTO.getTotalGeneral() != null ? ventaDTO.getTotalGeneral() : BigDecimal.ZERO;
        return new NotificacionDTO(
                tipo,
                mensaje,
                ventaDTO.getId(),
                total,
                ventaDTO.getUsuarioUsername(),
                LocalDateTime.now(),
                estado
        );
    }

    public void notificarNuevaVenta(VentaDTO ventaDTO) {
        BigDecimal total = ventaDTO.getTotalGeneral() != null ? ventaDTO.getTotalGeneral() : BigDecimal.ZERO;

        NotificacionDTO notificacion = new NotificacionDTO(
                "NUEVA_VENTA",
                "Nuevo pedido #" + ventaDTO.getId() + " - $" + total,
                ventaDTO.getId(),
                total,
                ventaDTO.getUsuarioUsername(),
                LocalDateTime.now(),
                ventaDTO.getEstado() != null ? ventaDTO.getEstado().name() : null
        );

        log.info("Enviando notificacion WebSocket: {}", notificacion.getMensaje());
        messagingTemplate.convertAndSend("/topic/ventas", notificacion);
    }

    public void notificarPedidoTomado(VentaDTO ventaDTO, String username) {
        NotificacionDTO notificacion = buildNotificacion(
                "PEDIDO_TOMADO",
                "¡Tu pedido #" + ventaDTO.getId() + " ha sido tomado y está en preparación!",
                ventaDTO,
                ventaDTO.getEstado().name()
        );

        log.info("Notificando a {}: {}", username, notificacion.getMensaje());
        messagingTemplate.convertAndSendToUser(username, "/topic/pedidos", notificacion);
    }

    public void notificarPedidoEnviado(VentaDTO ventaDTO, String username) {
        NotificacionDTO notificacion = buildNotificacion(
                "ENVIADO",
                "¡Tu pedido #" + ventaDTO.getId() + " ha sido enviado y va en camino!",
                ventaDTO,
                ventaDTO.getEstado().name()
        );

        log.info("Notificando a {}: {}", username, notificacion.getMensaje());
        messagingTemplate.convertAndSendToUser(username, "/topic/pedidos", notificacion);
    }

    public void notificarPedidoCompletado(VentaDTO ventaDTO, String username) {
        NotificacionDTO notificacion = buildNotificacion(
                "COMPLETADO",
                "¡Tu pedido #" + ventaDTO.getId() + " ha sido completado!",
                ventaDTO,
                ventaDTO.getEstado().name()
        );

        log.info("Notificando a {}: {}", username, notificacion.getMensaje());
        messagingTemplate.convertAndSendToUser(username, "/topic/pedidos", notificacion);
    }

    public void notificarPedidoCancelado(VentaDTO ventaDTO, String username) {
        NotificacionDTO notificacion = buildNotificacion(
                "CANCELADA",
                "Tu pedido #" + ventaDTO.getId() + " ha sido cancelado.",
                ventaDTO,
                ventaDTO.getEstado().name()
        );

        log.info("Notificando a {}: {}", username, notificacion.getMensaje());
        messagingTemplate.convertAndSendToUser(username, "/topic/pedidos", notificacion);
    }

    public void notificarCancelacionCliente(VentaDTO ventaDTO, String username) {
        String msg = "El cliente " + username + " canceló el pedido #" + ventaDTO.getId();
        NotificacionDTO notificacion = new NotificacionDTO(
                "CANCELADA_CLIENTE",
                msg,
                ventaDTO.getId(),
                ventaDTO.getTotalGeneral() != null ? ventaDTO.getTotalGeneral() : BigDecimal.ZERO,
                username,
                LocalDateTime.now(),
                ventaDTO.getEstado().name()
        );

        log.info("Enviando notificacion a admin: {}", notificacion.getMensaje());
        messagingTemplate.convertAndSend("/topic/ventas", notificacion);
    }
}
