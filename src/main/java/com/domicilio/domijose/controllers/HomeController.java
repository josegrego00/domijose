package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.ProductDTO;
import com.domicilio.domijose.services.ProductService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private final ProductService productService;
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<ProductDTO> productos = productService.getAllAvailableProducts();
        for (ProductDTO p : productos) {
            log.debug("Producto: {}, ID: {}, Nombre: {}", p, p.getId(), p.getName());
        }
        model.addAttribute("productos", productos);
        return "index";
    }
}