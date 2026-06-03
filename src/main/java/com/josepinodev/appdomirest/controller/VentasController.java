package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import com.josepinodev.appdomirest.model.EVentaEstado;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VentasController {

    private final VentaService ventaService;

    @GetMapping("/mis-pedidos")
    public String misPedidos(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "todos") String filtro,
                              @RequestParam(required = false) String success) {
        log.debug("=== MIS PEDIDOS === filtro: {}, page: {}", filtro, page);

        if (userDetails == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<VentaDTO> ventas = ventaService.getVentasUsuario(userDetails.getId(), pageable, filtro);

        model.addAttribute("ventas", ventas);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ventas.getTotalPages());
        model.addAttribute("filtro", filtro);

        if ("true".equals(success)) {
            model.addAttribute("successMessage", "Pedido realizado exitosamente");
        }

        return "store/mis-pedidos";
    }

    @GetMapping("/pedido/{id}/confirmado")
    public String pedidoConfirmado(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails,
                                    Model model) {
        log.debug("=== PEDIDO CONFIRMADO === id: {}", id);

        VentaDTO venta = ventaService.getVentaById(id);

        if (!venta.getUsuarioId().equals(userDetails.getId())) {
            return "redirect:/mis-pedidos";
        }

        model.addAttribute("venta", venta);
        return "store/pedido-confirmado";
    }

    @GetMapping("/admin/ventas")
    public String adminVentas(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String estado) {
        log.debug("=== ADMIN VENTAS === estado: {}", estado);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));

        Page<VentaDTO> ventas;
        if (estado != null && !estado.isEmpty()) {
            EVentaEstado estadoEnum = EVentaEstado.valueOf(estado.toUpperCase());
            ventas = ventaService.getVentasByEstado(estadoEnum, pageable);
        } else {
            ventas = ventaService.getAllVentas(pageable);
        }

        model.addAttribute("ventas", ventas);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ventas.getTotalPages());
        model.addAttribute("estadoFilter", estado);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());

        return "admin/ventas-list";
    }

    @GetMapping("/admin/ventas/{id}")
    public String ventaDetalle(@PathVariable Long id, Model model) {
        log.debug("=== VENTA DETALLE === id: {}", id);

        VentaDTO venta = ventaService.getVentaById(id);
        model.addAttribute("venta", venta);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());

        return "admin/ventas-detail";
    }

    @PostMapping("/admin/ventas/{id}/tomar")
    public String tomarPedido(@PathVariable Long id, Model model) {
        log.debug("=== TOMAR PEDIDO === id: {}", id);

        try {
            ventaService.tomarPedido(id);
            model.addAttribute("successMessage", "Pedido tomado, enviado a preparacion");
        } catch (Exception e) {
            log.error("Error al tomar pedido: ", e);
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/admin/ventas/" + id;
    }

    @PostMapping("/admin/ventas/{id}/enviar")
    public String enviarPedido(@PathVariable Long id, Model model) {
        log.debug("=== ENVIAR PEDIDO === id: {}", id);

        try {
            ventaService.enviarPedido(id);
            model.addAttribute("successMessage", "Pedido enviado a domicilio");
        } catch (Exception e) {
            log.error("Error al enviar pedido: ", e);
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/admin/ventas/" + id;
    }

    @PostMapping("/admin/ventas/{id}/completar")
    public String completarVenta(@PathVariable Long id, Model model) {
        log.debug("=== COMPLETAR VENTA === id: {}", id);

        try {
            ventaService.completarVenta(id);
            model.addAttribute("successMessage", "Pedido completado");
        } catch (Exception e) {
            log.error("Error al completar pedido: ", e);
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/admin/ventas/" + id;
    }

    @PostMapping("/pedido/{id}/cancelar")
    public String cancelarVentaCliente(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.debug("=== CANCELAR VENTA CLIENTE === id: {}", id);

        try {
            ventaService.cancelarVentaCliente(id, userDetails.getId());
        } catch (Exception e) {
            log.error("Error al cancelar pedido: ", e);
        }

        return "redirect:/mis-pedidos";
    }

    @PostMapping("/admin/ventas/{id}/cancelar")
    public String cancelarVenta(@PathVariable Long id, Model model) {
        log.debug("=== CANCELAR VENTA === id: {}", id);

        try {
            ventaService.cancelarVenta(id);
            model.addAttribute("successMessage", "Pedido cancelado");
        } catch (Exception e) {
            log.error("Error al cancelar pedido: ", e);
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/admin/ventas/" + id;
    }

    @GetMapping("/admin/ventas/pendientes/count")
    @ResponseBody
    public ResponseEntity<Long> countPendientes() {
        return ResponseEntity.ok(ventaService.countPendientes());
    }
}
