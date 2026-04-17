package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.models.User;
import com.domicilio.domijose.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/mis-pedidos")
    public String myOrders(Model model, @AuthenticationPrincipal User user) {
        log.info("Cargando pedidos para usuario: {}", user.getEmail());
        List<OrderDTO> orders = orderService.getOrdersByUserId(user.getId());
        model.addAttribute("pedidos", orders);
        return "pedidos/mis-pedidos";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        log.info("Cargando detalle de pedido ID: {} para usuario: {}", id, user.getEmail());
        
        OrderDTO order = orderService.getOrderByIdAndUserId(id, user.getId())
                .orElse(null);
        
        if (order == null) {
            log.error("Pedido no encontrado: {}", id);
            return "redirect:/pedidos/mis-pedidos?error=notfound";
        }
        
        model.addAttribute("pedido", order);
        return "pedidos/detalle";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelOrder(@PathVariable Long id, @AuthenticationPrincipal User user) {
        log.info("Cancelando pedido ID: {} para usuario: {}", id, user.getEmail());
        
        try {
            orderService.cancelOrder(id, user.getId());
            return "redirect:/pedidos/mis-pedidos?cancelled";
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error al cancelar: {}", e.getMessage());
            return "redirect:/pedidos/mis-pedidos?error=" + e.getMessage();
        }
    }
}