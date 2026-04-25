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

@Controller
@RequestMapping("/admin/productos")
public class AdminProductController {
    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(Model model, @RequestParam(required = false) String success, 
                               @RequestParam(required = false) String updated,
                               @RequestParam(required = false) String deleted,
                               @RequestParam(required = false) String activated) {
        log.info("Cargando lista de productos para admin");
        
        // ✅ Usar el método correcto
        java.util.List<ProductDTO> productos = productService.getAllProductsForAdmin();
        model.addAttribute("productos", productos);
        
        // Agregar mensajes de feedback
        if (success != null) model.addAttribute("success", "Producto creado exitosamente");
        if (updated != null) model.addAttribute("success", "Producto actualizado exitosamente");
        if (deleted != null) model.addAttribute("success", "Producto desactivado exitosamente");
        if (activated != null) model.addAttribute("success", "Producto activado exitosamente");
        
        return "admin/productos/lista";
    }

    @GetMapping("/nuevo")
    public String newProductForm(Model model) {
        log.info("Mostrando formulario para nuevo producto");
        model.addAttribute("productDTO", new ProductDTO());  // ✅ Nombre consistente
        return "admin/productos/form";
    }

    @PostMapping("/guardar")
    public String saveProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO, 
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Errores de validación al crear producto: {}", result.getAllErrors());
            model.addAttribute("productDTO", productDTO);
            return "admin/productos/form";
        }
        
        try {
            log.info("Guardando nuevo producto: {}", productDTO.getName());
            // ✅ Usar el método correcto: createProduct
            productService.createProduct(productDTO);
            return "redirect:/admin/productos?success";
        } catch (IllegalArgumentException e) {
            log.error("Error al guardar producto: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productDTO", productDTO);
            return "admin/productos/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Mostrando formulario para editar producto ID: {}", id);
        
        try {
            // ✅ Usar el método correcto
            ProductDTO productDTO = productService.getProductByIdForAdmin(id);
            model.addAttribute("productDTO", productDTO);
            return "admin/productos/form";
        } catch (IllegalArgumentException e) {
            log.error("Producto no encontrado: {}", id);
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/admin/productos";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Errores de validación al actualizar producto: {}", result.getAllErrors());
            model.addAttribute("productDTO", productDTO);
            return "admin/productos/form";
        }
        
        try {
            log.info("Actualizando producto ID: {}", id);
            // ✅ El método updateProduct ya recibe (id, dto)
            productService.updateProduct(id, productDTO);
            return "redirect:/admin/productos?updated";
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar producto: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productDTO", productDTO);
            return "admin/productos/form";
        }
    }

    @GetMapping("/desactivar/{id}")
    public String deactivateProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Desactivando producto ID: {}", id);
        try {
            productService.deleteProduct(id);
            return "redirect:/admin/productos?deleted";
        } catch (IllegalArgumentException e) {
            log.error("Error al desactivar producto: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/productos";
        }
    }

    @GetMapping("/activar/{id}")
    public String activateProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Activando producto ID: {}", id);
        try {
            productService.activateProduct(id);
            return "redirect:/admin/productos?activated";
        } catch (IllegalArgumentException e) {
            log.error("Error al activar producto: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/productos";
        }
    }
}