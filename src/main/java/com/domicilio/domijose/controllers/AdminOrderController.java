package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.models.enums.OrderStatus;
import com.domicilio.domijose.services.OrderService;
import com.domicilio.domijose.services.WhatsAppLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminOrderController {
    private static final Logger log = LoggerFactory.getLogger(AdminOrderController.class);
    private static final int PAGE_SIZE = 10;

    private final OrderService orderService;
    private final WhatsAppLinkService whatsAppLinkService;

    public AdminOrderController(OrderService orderService, WhatsAppLinkService whatsAppLinkService) {
        this.orderService = orderService;
        this.whatsAppLinkService = whatsAppLinkService;
    }

    @GetMapping
    public String listOrders(Model model,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                            @RequestParam(defaultValue = "0") int page) {
        LocalDate fechaFiltro = (fecha != null) ? fecha : LocalDate.now();
        log.info("Cargando pedidos para admin con fecha: {}, página: {}", fechaFiltro, page);

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<OrderDTO> ordersPage = orderService.getOrdersByDateWithPagination(fechaFiltro, pageable);

        model.addAttribute("pedidosPage", ordersPage);
        model.addAttribute("fechaFiltro", fechaFiltro);
        model.addAttribute("currentPage", page);

        return "admin/pedidos/lista";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        log.info("Cargando detalle de pedido ID: {}", id);
        OrderDTO order = orderService.getOrderById(id);
        model.addAttribute("pedido", order);

        String whatsappClienteLink = whatsAppLinkService.generarLinkParaCliente(order);
        model.addAttribute("whatsappClienteLink", whatsappClienteLink);

        return "admin/pedidos/detalle";
    }

    @PostMapping("/{id}/confirmar")
    public String confirmOrder(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        log.info("Confirmando pedido ID: {}", id);
        try {
            OrderDTO order = orderService.confirmOrder(id);

            String whatsappClienteLink = whatsAppLinkService.generarLinkParaCliente(order);
            model.addAttribute("whatsappClienteLink", whatsappClienteLink);

            redirectAttributes.addFlashAttribute("success", "Pedido confirmado");
            return "redirect:/admin/pedidos/" + id;
        } catch (IllegalStateException e) {
            log.error("Error al confirmar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/pedidos/" + id;
        }
    }

    @PostMapping("/{id}/preparacion")
    public String startPreparation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Iniciando preparación pedido ID: {}", id);
        orderService.updateOrderStatus(id, OrderStatus.EN_PREPARACION);
        redirectAttributes.addFlashAttribute("success", "Preparación iniciada");
        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/{id}/en-camino")
    public String sendToDelivery(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Enviando a domicilio pedido ID: {}", id);
        orderService.updateOrderStatus(id, OrderStatus.EN_CAMINO);
        redirectAttributes.addFlashAttribute("success", "Pedido en camino");
        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/{id}/entregado")
    public String markDelivered(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Marcando como entregado pedido ID: {}", id);
        orderService.updateOrderStatus(id, OrderStatus.ENTREGADO);
        redirectAttributes.addFlashAttribute("success", "Pedido entregado");
        return "redirect:/admin/pedidos/" + id;
    }
}