package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.ProductDTO;
import com.domicilio.domijose.services.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ========== ENDPOINTS PÚBLICOS (CLIENTE) ==========
    
    @GetMapping
    public String listProducts(Model model) {
        List<ProductDTO> products = productService.getAllAvailableProducts();
        model.addAttribute("productos", products);
        return "productos/lista";
    }

        @GetMapping("/{id}")
        public String productDetail(@PathVariable Long id, Model model) {
            try {
                ProductDTO product = productService.getProductByIdForPublic(id);
                model.addAttribute("producto", product);
                return "productos/detalle";
            } catch (IllegalArgumentException e) {
                log.warn("Producto no encontrado o no disponible: {}", id);
                return "redirect:/productos";
            }
        }

    @GetMapping("/buscar")
    public String searchProducts(@RequestParam String q, Model model) {
        List<ProductDTO> products = productService.searchProducts(q);
        model.addAttribute("productos", products);
        model.addAttribute("busqueda", q);
        return "productos/lista";
    }

}