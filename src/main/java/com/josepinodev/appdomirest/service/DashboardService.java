package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.DashboardDTO;
import com.josepinodev.appdomirest.dto.DashboardUpdateEvent;
import com.josepinodev.appdomirest.model.EVentaEstado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ProductoService productoService;
    private final UserService userService;
    private final VentaService ventaService;

    public DashboardDTO getDashboardDTO() {
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalProductos(productoService.countActive());
        dto.setTotalUsuarios(userService.countActive());
        dto.setProductosPromocion(productoService.findEnPromocion().size());
        dto.setCantidadPendientes(ventaService.countPendientes());
        dto.setVentasPendientes(
                ventaService.getVentasByEstado(EVentaEstado.PENDIENTE, PageRequest.of(0, 5)).getContent());
        return dto;
    }

    @EventListener
    @Async
    public void onDashboardUpdate(DashboardUpdateEvent event) {
        log.debug("DashboardUpdateEvent recibido, actualizando dashboard...");
        DashboardDTO dto = getDashboardDTO();
        messagingTemplate.convertAndSend("/topic/dashboard", dto);
    }
}
