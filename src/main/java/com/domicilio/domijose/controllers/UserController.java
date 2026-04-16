package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.UserDTO;
import com.domicilio.domijose.dto.UserRequest;
import com.domicilio.domijose.services.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registro")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRequest", new UserRequest());
        return "registro";
    }

    @PostMapping("/registro")
    public String register(@Valid @ModelAttribute UserRequest userRequest, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "registro";
        }
        try {
            userService.create(userRequest);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
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
        return "perfil";
    }
}