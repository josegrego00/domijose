package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.ProductDTO;
import com.domicilio.domijose.services.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/productos")
public class AdminProductController {
    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(Model model) {
        log.info("Cargando lista de productos para admin");
        List<ProductDTO> productos = productService.getAllProducts();
        model.addAttribute("productos", productos);
        return "admin/productos/lista";
    }

    @GetMapping("/nuevo")
    public String newProductForm(Model model) {
        log.info("Mostrando formulario para nuevo producto");
        model.addAttribute("producto", new ProductDTO());
        return "admin/productos/form";
    }

    @PostMapping("/guardar")
    public String saveProduct(@Valid @ModelAttribute ProductDTO producto, Model model) {
        try {
            log.info("Guardando nuevo producto: {}", producto.getName());
            productService.saveProduct(producto);
            return "redirect:/admin/productos?success";
        } catch (IllegalArgumentException e) {
            log.error("Error al guardar producto: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("producto", producto);
            return "admin/productos/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        log.info("Mostrando formulario para editar producto ID: {}", id);
        ProductDTO producto = productService.getProductByIdForAdmin(id)
                .orElse(null);
        
        if (producto == null) {
            log.error("Producto no encontrado: {}", id);
            return "redirect:/admin/productos?error=notfound";
        }
        
        model.addAttribute("producto", producto);
        return "admin/productos/form";
    }

    @PostMapping("/actualizar")
    public String updateProduct(@Valid @ModelAttribute ProductDTO producto, Model model) {
        try {
            if (producto.getId() == null) {
                log.error("ID de producto es null en actualización");
                return "redirect:/admin/productos?error=invalid";
            }
            log.info("Actualizando producto ID: {}", producto.getId());
            productService.updateProduct(producto.getId(), producto);
            return "redirect:/admin/productos?updated";
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar producto: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("producto", producto);
            return "admin/productos/form";
        }
    }

    @GetMapping("/desactivar/{id}")
    public String deactivateProduct(@PathVariable Long id) {
        log.info("Desactivando producto ID: {}", id);
        productService.deleteProduct(id);
        return "redirect:/admin/productos?deleted";
    }

    @GetMapping("/activar/{id}")
    public String activateProduct(@PathVariable Long id) {
        log.info("Activando producto ID: {}", id);
        productService.activateProduct(id);
        return "redirect:/admin/productos?activated";
    }
}