package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.user.UserRequest;
import com.josepinodev.appdomirest.dto.user.UserResponse;
import com.josepinodev.appdomirest.service.UserService;
import com.josepinodev.appdomirest.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final VentaService ventaService;

    @GetMapping("/admin/usuarios")
    public String listarClientes(
            @RequestParam(required = false) String q,
            Model model) {
        log.debug("=== CLIENTES LIST - INICIO ===");

        List<UserResponse> clientes;
        if (q != null && !q.trim().isEmpty()) {
            log.debug("Buscando clientes con query: {}", q);
            clientes = userService.searchCustomers(q.trim());
        } else {
            clientes = userService.findAllCustomers();
        }

        Long totalClientes = userService.countCustomers();
        Long clientesActivos = userService.countCustomersActive();
        Long clientesBloqueados = userService.countCustomersBlocked();

        model.addAttribute("clientes", clientes);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("clientesActivos", clientesActivos);
        model.addAttribute("clientesBloqueados", clientesBloqueados);
        model.addAttribute("searchQuery", q);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());

        log.debug("clientes.size: {}", clientes.size());
        return "admin/usuarios-list";
    }

    @PostMapping("/admin/usuarios/{id}/toggle")
    public String toggleCliente(@PathVariable Long id) {
        log.debug("Toggle cliente id: {}", id);
        userService.toggleActivo(id);
        return "redirect:/admin/usuarios?toggled";
    }

    @GetMapping("/admin/admins")
    public String listarAdmins(Model model) {
        log.debug("=== ADMINS LIST - INICIO ===");

        List<UserResponse> admins = userService.findAllAdmins();
        model.addAttribute("admins", admins);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());

        log.debug("admins.size: {}", admins.size());
        return "admin/admins-list";
    }

    @GetMapping("/admin/admins/nuevo")
    public String nuevoAdmin(Model model) {
        model.addAttribute("admin", new UserRequest());
        model.addAttribute("esNuevo", true);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        return "admin/admin/admin-form";
    }

    @PostMapping("/admin/admins")
    public String guardarAdmin(@ModelAttribute("admin") UserRequest request, Model model) {
        log.debug("Guardando nuevo admin: {}", request.getUsername());
        try {
            userService.saveAdmin(request);
            return "redirect:/admin/admins?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("admin", request);
            model.addAttribute("esNuevo", true);
            model.addAttribute("cantidadPendientes", ventaService.countPendientes());
            return "admin/admin/admin-form";
        }
    }

    @GetMapping("/admin/admins/{id}/editar")
    public String editarAdmin(@PathVariable Long id, Model model) {
        UserResponse admin = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));
        model.addAttribute("admin", admin);
        model.addAttribute("esNuevo", false);
        model.addAttribute("cantidadPendientes", ventaService.countPendientes());
        return "admin/admin/admin-form";
    }

    @PostMapping("/admin/admins/{id}")
    public String actualizarAdmin(@PathVariable Long id, @ModelAttribute("admin") UserRequest request, Model model) {
        log.debug("Actualizando admin id: {}", id);
        try {
            userService.updateAdmin(request, id);
            return "redirect:/admin/admins?updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("admin", request);
            model.addAttribute("esNuevo", false);
            model.addAttribute("cantidadPendientes", ventaService.countPendientes());
            return "admin/admin/admin-form";
        }
    }

    @PostMapping("/admin/admins/{id}/eliminar")
    public String eliminarAdmin(@PathVariable Long id) {
        log.debug("Eliminando admin id: {}", id);
        userService.delete(id);
        return "redirect:/admin/admins?deleted";
    }
}