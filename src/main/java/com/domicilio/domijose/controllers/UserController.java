package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.UserDTO;
import com.domicilio.domijose.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registro")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String register(@Valid @ModelAttribute("userDTO") UserDTO userDTO, 
                           BindingResult result, 
                           Model model) {
        if (result.hasErrors()) {
            log.warn("Errores de validación en registro: {}", result.getAllErrors());
            return "registro";
        }
        
        try {
            userService.registerUser(userDTO);
            log.info("Registro exitoso para teléfono: {}", userDTO.getPhone());
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            log.error("Error en registro: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String registered, Model model) {
        if (registered != null) {
            model.addAttribute("message", "Registro exitoso. Por favor, inicia sesión.");
        }
        return "login";
    }

    @GetMapping("/perfil")
    public String profile(Model model) {
        // TODO: Obtener usuario autenticado y mostrar sus datos
        return "perfil";
    }
}