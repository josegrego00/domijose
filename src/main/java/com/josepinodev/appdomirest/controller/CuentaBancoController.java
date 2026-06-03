package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.cuenta.CuentaBancoDTO;
import com.josepinodev.appdomirest.service.CuentaBancoService;
import com.josepinodev.appdomirest.service.ImageService;
import com.josepinodev.appdomirest.service.VentaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/cuentas-banco")
@RequiredArgsConstructor
@Slf4j
public class CuentaBancoController {

    private final CuentaBancoService cuentaBancoService;
    private final VentaService ventaService;
    private final ImageService imageService;

    @GetMapping
    public String listarCuentas(Model model) {
        List<CuentaBancoDTO> cuentas = cuentaBancoService.findAll();
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        return "admin/cuentas-banco-list";
    }

    @GetMapping("/nuevo")
    public String nuevaCuenta(Model model) {
        model.addAttribute("cuenta", new CuentaBancoDTO());
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        return "admin/cuenta-banco/cuenta-form";
    }

    @PostMapping
    public String guardarCuenta(@Valid @ModelAttribute("cuenta") CuentaBancoDTO dto,
                                 @RequestParam("qrFile") MultipartFile qrFile) {
        log.debug("Guardando nueva cuenta bancaria: {}", dto.getBanco());
        if (qrFile != null && !qrFile.isEmpty()) {
            String qrUrl = imageService.saveImage(qrFile);
            dto.setImagenQrUrl(qrUrl);
        }
        cuentaBancoService.save(dto);
        return "redirect:/admin/cuentas-banco?success";
    }

    @GetMapping("/{id}/editar")
    public String editarCuenta(@PathVariable Long id, Model model) {
        CuentaBancoDTO cuenta = cuentaBancoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));
        model.addAttribute("cuenta", cuenta);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        return "admin/cuenta-banco/cuenta-form";
    }

    @PostMapping("/{id}")
    public String actualizarCuenta(@PathVariable Long id,
                                    @Valid @ModelAttribute("cuenta") CuentaBancoDTO dto,
                                    @RequestParam("qrFile") MultipartFile qrFile) {
        log.debug("Actualizando cuenta bancaria id: {}", id);
        CuentaBancoDTO existing = cuentaBancoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));
        if (qrFile != null && !qrFile.isEmpty()) {
            if (existing.getImagenQrUrl() != null) {
                imageService.deleteImage(existing.getImagenQrUrl());
            }
            String qrUrl = imageService.saveImage(qrFile);
            dto.setImagenQrUrl(qrUrl);
        } else {
            dto.setImagenQrUrl(existing.getImagenQrUrl());
        }
        dto.setId(id);
        cuentaBancoService.update(dto);
        return "redirect:/admin/cuentas-banco?updated";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarCuenta(@PathVariable Long id) {
        log.debug("Eliminando cuenta bancaria id: {}", id);
        cuentaBancoService.delete(id);
        return "redirect:/admin/cuentas-banco?deleted";
    }
}
