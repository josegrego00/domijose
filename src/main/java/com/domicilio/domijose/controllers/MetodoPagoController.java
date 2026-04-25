package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.MetodoPagoDTO;
import com.domicilio.domijose.services.MetodoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/metodo-pago")
public class MetodoPagoController {
    private static final Logger log = LoggerFactory.getLogger(MetodoPagoController.class);

    private final MetodoPagoService metodoPagoService;

    public MetodoPagoController(MetodoPagoService metodoPagoService) {
        this.metodoPagoService = metodoPagoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cuentas", metodoPagoService.getCuentas());
        model.addAttribute("qrs", metodoPagoService.getQrs());
        return "admin/metodo-pago/lista";
    }

    @GetMapping("/cuenta/nueva")
    public String formCuenta(Model model) {
        model.addAttribute("metodoPago", new MetodoPagoDTO());
        return "admin/metodo-pago/form-cuenta";
    }

    @PostMapping("/cuenta")
    public String guardarCuenta(@ModelAttribute MetodoPagoDTO dto, RedirectAttributes redirectAttributes) {
        metodoPagoService.saveCuenta(dto);
        redirectAttributes.addFlashAttribute("success", "Cuenta guardada");
        return "redirect:/admin/metodo-pago";
    }

    @PostMapping("/cuenta/{id}/eliminar")
    public String eliminarCuenta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        metodoPagoService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Cuenta eliminada");
        return "redirect:/admin/metodo-pago";
    }

    @GetMapping("/qr/nuevo")
    public String formQr(Model model) {
        model.addAttribute("metodoPago", new MetodoPagoDTO());
        return "admin/metodo-pago/form-qr";
    }

    @PostMapping("/qr")
    public String guardarQr(@ModelAttribute MetodoPagoDTO dto,
            @RequestParam("qrFile") MultipartFile qrFile,
            RedirectAttributes redirectAttributes) {
        dto.setQrFile(qrFile);
        metodoPagoService.saveQr(dto);
        redirectAttributes.addFlashAttribute("success", "QR guardado");
        return "redirect:/admin/metodo-pago";
    }

    @PostMapping("/qr/{id}/eliminar")
    public String eliminarQr(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        metodoPagoService.delete(id);
        redirectAttributes.addFlashAttribute("success", "QR eliminado");
        return "redirect:/admin/metodo-pago";
    }
}