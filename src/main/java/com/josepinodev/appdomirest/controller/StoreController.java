package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import com.josepinodev.appdomirest.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final ProductoService productoService;

    @GetMapping("/")
    public String index(Model model) {
        log.debug("=== STORE INDEX - INICIO ===");

        List<ProductoDTO> productosDestacados = productoService.findEnPromocion();
        if (productosDestacados.isEmpty()) {
            productosDestacados = productoService.findAllActive().stream().limit(8).toList();
        }

        model.addAttribute("productosDestacados", productosDestacados);
        return "index";
    }

    @GetMapping("/catalogo")
    public String catalogo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        log.debug("=== CATALOGO - INICIO ===");
        log.debug("page: {}, size: {}", page, size);

        Page<ProductoDTO> productos = productoService.findAllPaginated(page, size);
        model.addAttribute("productos", productos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productos.getTotalPages());

        log.debug("productos.getTotalElements: {}", productos.getTotalElements());
        return "store/catalogo";
    }

    @GetMapping("/producto/{id}")
    public String productoDetalle(@PathVariable Long id, Model model) {
        log.debug("=== PRODUCTO DETALLE - INICIO ===");
        log.debug("id: {}", id);

        ProductoDTO producto = productoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        model.addAttribute("producto", producto);
        return "store/producto-detalle";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam String q, Model model) {
        log.debug("=== BUSCAR - INICIO ===");
        log.debug("query: {}", q);

        List<ProductoDTO> resultados = productoService.searchByNombre(q);

        model.addAttribute("resultados", resultados);
        model.addAttribute("query", q);

        log.debug("resultados.size: {}", resultados.size());
        return "store/buscar";
    }
}