package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.ProductDTO;
import com.domicilio.domijose.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(Model model) {
        List<ProductDTO> products = productService.getAllAvailableProducts();
        model.addAttribute("productos", products);
        return "productos/lista";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getProductById(id)
                .orElse(null);
        
        if (product == null) {
            return "redirect:/productos";
        }
        
        model.addAttribute("producto", product);
        return "productos/detalle";
    }

    @GetMapping("/buscar")
    public String searchProducts(@RequestParam String q, Model model) {
        List<ProductDTO> products = productService.searchProducts(q);
        model.addAttribute("productos", products);
        model.addAttribute("busqueda", q);
        return "productos/lista";
    }
}