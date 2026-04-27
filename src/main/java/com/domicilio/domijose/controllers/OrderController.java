package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.dto.OrderItemDTO;
import com.domicilio.domijose.services.security.CustomUserDetails;
import com.domicilio.domijose.services.OrderService;
import com.domicilio.domijose.services.WhatsAppLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final WhatsAppLinkService whatsAppLinkService;

    public OrderController(OrderService orderService, WhatsAppLinkService whatsAppLinkService) {
        this.orderService = orderService;
        this.whatsAppLinkService = whatsAppLinkService;
    }

    @GetMapping("/mis-pedidos")
    public String myOrders(Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        LocalDate fechaFiltro = (fecha != null) ? fecha : LocalDate.now();
        log.info("Cargando pedidos para usuario: {} con fecha: {}", userDetails.getPhone(), fechaFiltro);

        List<OrderDTO> orders = orderService.getOrdersByUserIdAndDateRange(userDetails.getUser().getId(), fechaFiltro);
        model.addAttribute("pedidos", orders);
        model.addAttribute("fechaFiltro", fechaFiltro);
        return "pedidos/mis-pedidos";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model, 
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        log.info("Cargando detalle de pedido ID: {} para usuario: {}", id, userDetails.getPhone());
        
        try {
            OrderDTO order = orderService.getOrderByIdAndUserId(id, userDetails.getUser().getId());
            model.addAttribute("pedido", order);
            
            // Generar enlace WhatsApp para cliente solo si el pedido está confirmado
            if (order.getStatus() != null && order.getStatus().name().equals("CONFIRMADO")) {
                String whatsappLink = whatsAppLinkService.generarLinkParaCliente(order);
                model.addAttribute("whatsappLink", whatsappLink);
            }
            
            return "pedidos/detalle";
        } catch (IllegalArgumentException e) {
            log.error("Pedido no encontrado: {}", id);
            redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
            return "redirect:/pedidos/mis-pedidos";
        }
    }

    @PostMapping("/{id}/cancelar")
    public String cancelOrder(@PathVariable Long id, 
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        log.info("Cancelando pedido ID: {} para usuario: {}", id, userDetails.getPhone());
        
        try {
            orderService.cancelOrder(id, userDetails.getUser().getId());
            redirectAttributes.addFlashAttribute("success", "Pedido cancelado exitosamente");
            return "redirect:/pedidos/mis-pedidos";
        } catch (IllegalArgumentException e) {
            log.error("Error al cancelar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/mis-pedidos";
        } catch (IllegalStateException e) {
            log.error("Error al cancelar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/" + id;
        }
    }
}