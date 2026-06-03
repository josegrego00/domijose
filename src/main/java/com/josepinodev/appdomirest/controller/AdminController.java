package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.service.ProductoService;
import com.josepinodev.appdomirest.service.UserService;
import com.josepinodev.appdomirest.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ProductoService productoService;
    private final UserService userService;
    private final VentaService ventaService;

    @GetMapping
    public String dashboard(Model model) {
        log.debug("=== DASHBOARD /admin - INICIO ===");
        log.debug("Obteniendo totalProductos...");
        long totalProductos = productoService.countActive();
        log.debug("totalProductos: {}", totalProductos);

        log.debug("Obteniendo totalUsuarios...");
        long totalUsuarios = userService.countActive();
        log.debug("totalUsuarios: {}", totalUsuarios);

        log.debug("Obteniendo productosPromocion...");
        var productosPromocion = productoService.findEnPromocion();
        log.debug("productosPromocion.size: {}", productosPromocion.size());

        log.debug("Obteniendo pedidos pendientes...");
        long cantidadPendientes = ventaService.countPendientes();
        log.debug("cantidadPendientes: {}", cantidadPendientes);

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("productosPromocion", productosPromocion);
        model.addAttribute("cantidadPendientes", cantidadPendientes);

        log.debug("Retornando vista: admin/home-admin");
        return "admin/home-admin";
    }
}