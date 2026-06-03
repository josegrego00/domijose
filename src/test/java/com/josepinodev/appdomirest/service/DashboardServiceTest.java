package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.DashboardDTO;
import com.josepinodev.appdomirest.dto.DashboardUpdateEvent;
import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ProductoService productoService;
    @Mock private UserService userService;
    @Mock private VentaService ventaService;
    @InjectMocks private DashboardService dashboardService;

    @Test
    void getDashboardDTO_ReturnsPopulatedDTO() {
        when(productoService.countActive()).thenReturn(10L);
        when(userService.countActive()).thenReturn(5L);
        when(productoService.findEnPromocion()).thenReturn(List.of());
        when(ventaService.countPendientes()).thenReturn(3L);
        when(ventaService.getVentasByEstado(any(), any())).thenReturn(new PageImpl<>(List.of()));

        DashboardDTO dto = dashboardService.getDashboardDTO();

        assertEquals(10L, dto.getTotalProductos());
        assertEquals(5L, dto.getTotalUsuarios());
        assertEquals(0, dto.getProductosPromocion());
        assertEquals(3L, dto.getCantidadPendientes());
        assertNotNull(dto.getVentasPendientes());
    }

    @Test
    void onDashboardUpdate_SendsMessage() {
        when(productoService.countActive()).thenReturn(10L);
        when(userService.countActive()).thenReturn(5L);
        when(productoService.findEnPromocion()).thenReturn(List.of());
        when(ventaService.countPendientes()).thenReturn(0L);
        when(ventaService.getVentasByEstado(any(), any())).thenReturn(new PageImpl<>(List.of()));

        dashboardService.onDashboardUpdate(new DashboardUpdateEvent(this));

        verify(messagingTemplate).convertAndSend(eq("/topic/dashboard"), any(DashboardDTO.class));
    }
}
