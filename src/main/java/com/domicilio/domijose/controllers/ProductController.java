package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.ProductDTO;
import com.domicilio.domijose.services.FileService;
import com.domicilio.domijose.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductController {
    private final ProductService productService;
    private final FileService fileService;

    public ProductController(ProductService productService, FileService fileService) {
        this.productService = productService;
        this.fileService = fileService;
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

    @GetMapping("/nuevo")
    public String newProductForm(Model model) {
        model.addAttribute("producto", new ProductDTO());
        return "productos/form";
    }

    @PostMapping
    public String createProduct(@Valid @ModelAttribute ProductDTO producto,
                                @RequestParam("imagen") MultipartFile imagen,
                                Model model) {
        try {
            String imageUrl = fileService.saveImage(imagen);
            producto.setImageUrl(imageUrl);
            productService.saveProduct(producto);
            return "redirect:/productos?success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("producto", producto);
            return "productos/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editProductForm(@PathVariable Long id, Model model) {
        ProductDTO producto = productService.getProductById(id)
                .orElse(null);
        
        if (producto == null) {
            return "redirect:/productos";
        }
        
        model.addAttribute("producto", producto);
        return "productos/form";
    }

    @PostMapping("/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute ProductDTO producto,
                                @RequestParam("imagen") MultipartFile imagen,
                                Model model) {
        try {
            if (!imagen.isEmpty()) {
                String imageUrl = fileService.saveImage(imagen);
                producto.setImageUrl(imageUrl);
            }
            productService.updateProduct(id, producto);
            return "redirect:/productos?updated";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("producto", producto);
            return "productos/form";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/productos?deleted";
    }
}