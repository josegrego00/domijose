package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import com.josepinodev.appdomirest.service.ImageService;
import com.josepinodev.appdomirest.service.ProductoService;
import com.josepinodev.appdomirest.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoService productoService;
    private final ImageService imageService;
    private final VentaService ventaService;

    @GetMapping
    public String listarProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        log.debug("=== PRODUCTOS LIST - INICIO ===");
        log.debug("page: {}, size: {}", page, size);

        log.debug("Obteniendo productos paginados...");
        Page<ProductoDTO> productos = productoService.findAllPaginated(page, size);
        log.debug("productos.getTotalElements: {}", productos.getTotalElements());
        log.debug("productos.getTotalPages: {}", productos.getTotalPages());
        log.debug("productos.getNumber: {}", productos.getNumber());

        model.addAttribute("productos", productos);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        log.debug("Retornando vista: admin/productos-list");
        return "admin/productos-list";
    }

    @GetMapping("/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        return "admin/producto/productos-form";
    }

    @PostMapping
    public String guardarProducto(
            @ModelAttribute("producto") ProductoDTO dto,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Model model) {

        if (imagen != null && !imagen.isEmpty()) {
            String imagenUrl = imageService.saveImage(imagen);
            if (imagenUrl != null) {
                dto.setImagenUrl(imagenUrl);
            }
        }

        productoService.save(dto);

        return "redirect:/admin/productos?success";
    }

    @GetMapping("/{id}/editar")
    public String editarProducto(@PathVariable Long id, Model model) {
        ProductoDTO producto = productoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        model.addAttribute("producto", producto);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        return "admin/producto/productos-form";
    }

    @PostMapping("/{id}")
    public String actualizarProducto(
            @PathVariable Long id,
            @ModelAttribute("producto") ProductoDTO productoDTO,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Model model) {

        ProductoDTO existing = productoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (imagen != null && !imagen.isEmpty()) {
            String nuevaImagenUrl = imageService.saveImage(imagen);
            if (nuevaImagenUrl != null) {
                if (existing.getImagenUrl() != null) {
                    imageService.deleteImage(existing.getImagenUrl());
                }
                productoDTO.setImagenUrl(nuevaImagenUrl);
            } else {
                productoDTO.setImagenUrl(existing.getImagenUrl());
            }
        } else {
            productoDTO.setImagenUrl(existing.getImagenUrl());
        }

        productoDTO.setId(id);
        productoDTO.setFechaCreacion(existing.getFechaCreacion());
        productoDTO.setUsuarioCreadorId(existing.getUsuarioCreadorId());

        productoService.update(productoDTO);

        return "redirect:/admin/productos?updated";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarProducto(@PathVariable Long id) {
        productoService.delete(id);
        return "redirect:/admin/productos?deleted";
    }
}