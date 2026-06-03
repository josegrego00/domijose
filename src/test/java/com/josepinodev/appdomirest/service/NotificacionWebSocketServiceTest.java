package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.NotificacionDTO;
import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import com.josepinodev.appdomirest.model.EVentaEstado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionWebSocketServiceTest {

    @Mock private SimpMessagingTemplate messagingTemplate;
    @InjectMocks private NotificacionWebSocketService notificacionWebSocketService;
    @Captor private ArgumentCaptor<NotificacionDTO> notificacionCaptor;

    private VentaDTO ventaDTO;

    @BeforeEach
    void setUp() {
        ventaDTO = new VentaDTO();
        ventaDTO.setId(1L);
        ventaDTO.setUsuarioId(1L);
        ventaDTO.setUsuarioUsername("testuser");
        ventaDTO.setEstado(EVentaEstado.PENDIENTE);
        ventaDTO.setTotal(new BigDecimal("10000"));
        ventaDTO.setSubtotal(new BigDecimal("10000"));
        ventaDTO.setEsDomicilio(false);
    }

    @Test
    void notificarNuevaVenta_SendsToTopicVentas() {
        notificacionWebSocketService.notificarNuevaVenta(ventaDTO);

        verify(messagingTemplate).convertAndSend(eq("/topic/ventas"), any(NotificacionDTO.class));
    }

    @Test
    void notificarPedidoTomado_SendsToUser() {
        notificacionWebSocketService.notificarPedidoTomado(ventaDTO, "testuser");

        verify(messagingTemplate).convertAndSendToUser(eq("testuser"), eq("/topic/pedidos"), any(NotificacionDTO.class));
    }

    @Test
    void notificarPedidoEnviado_SendsToUser() {
        notificacionWebSocketService.notificarPedidoEnviado(ventaDTO, "testuser");

        verify(messagingTemplate).convertAndSendToUser(eq("testuser"), eq("/topic/pedidos"), any(NotificacionDTO.class));
    }

    @Test
    void notificarPedidoCompletado_SendsToUser() {
        notificacionWebSocketService.notificarPedidoCompletado(ventaDTO, "testuser");

        verify(messagingTemplate).convertAndSendToUser(eq("testuser"), eq("/topic/pedidos"), any(NotificacionDTO.class));
    }

    @Test
    void notificarPedidoCancelado_SendsToUser() {
        notificacionWebSocketService.notificarPedidoCancelado(ventaDTO, "testuser");

        verify(messagingTemplate).convertAndSendToUser(eq("testuser"), eq("/topic/pedidos"), any(NotificacionDTO.class));
    }

    @Test
    void notificarCancelacionCliente_SendsToTopicVentas() {
        notificacionWebSocketService.notificarCancelacionCliente(ventaDTO, "testuser");

        verify(messagingTemplate).convertAndSend(eq("/topic/ventas"), any(NotificacionDTO.class));
    }

    @Test
    void notificarNuevaVenta_ContainsCorrectData() {
        notificacionWebSocketService.notificarNuevaVenta(ventaDTO);

        verify(messagingTemplate).convertAndSend(eq("/topic/ventas"), notificacionCaptor.capture());
        NotificacionDTO notif = notificacionCaptor.getValue();

        assertEquals("NUEVA_VENTA", notif.getTipo());
        assertEquals(1L, notif.getVentaId());
        assertEquals("testuser", notif.getUsuarioNombre());
        assertNotNull(notif.getTimestamp());
    }
}
