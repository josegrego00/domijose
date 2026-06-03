package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.ruta.RutaDomicilioDTO;
import com.josepinodev.appdomirest.service.RutaDomicilioService;
import com.josepinodev.appdomirest.service.VentaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/rutas")
@RequiredArgsConstructor
@Slf4j
public class RutaDomicilioController {

    private final RutaDomicilioService rutaDomicilioService;
    private final VentaService ventaService;

    @GetMapping
    public String listarRutas(Model model) {
        log.debug("=== RUTAS LIST - INICIO ===");

        List<RutaDomicilioDTO> rutas = rutaDomicilioService.findAll();
        rutas.sort((r1, r2) -> r1.getBarrio().compareToIgnoreCase(r2.getBarrio()));

        log.debug("rutas.size: {}", rutas.size());
        model.addAttribute("rutas", rutas);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        log.debug("Retornando vista: admin/rutas-list");
        return "admin/rutas-list";
    }

    @GetMapping("/nuevo")
    public String nuevaRuta(Model model) {
        model.addAttribute("ruta", new RutaDomicilioDTO());
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        return "admin/ruta/ruta-form";
    }

    @PostMapping
    public String guardarRuta(@Valid @ModelAttribute("ruta") RutaDomicilioDTO dto, Model model) {
        log.debug("Guardando nueva ruta: {}", dto.getBarrio());
        rutaDomicilioService.save(dto);
        return "redirect:/admin/rutas?success";
    }

    @GetMapping("/{id}/editar")
    public String editarRuta(@PathVariable Long id, Model model) {
        RutaDomicilioDTO ruta = rutaDomicilioService.findById(id)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));
        model.addAttribute("ruta", ruta);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        return "admin/ruta/ruta-form";
    }

    @PostMapping("/{id}")
    public String actualizarRuta(@PathVariable Long id, @Valid @ModelAttribute("ruta") RutaDomicilioDTO dto,
            Model model) {
        log.debug("Actualizando ruta id: {}", id);
        dto.setId(id);
        rutaDomicilioService.update(dto);
        return "redirect:/admin/rutas?updated";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarRuta(@PathVariable Long id) {
        log.debug("Eliminando ruta id: {}", id);
        rutaDomicilioService.delete(id);
        return "redirect:/admin/rutas?deleted";
    }
}