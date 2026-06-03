package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.user.UserRequest;
import com.josepinodev.appdomirest.service.AuditService;
import com.josepinodev.appdomirest.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuditService auditService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRequest());
        return "auth/registro";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserRequest request, 
                          @RequestParam String confirmPassword,
                          BindingResult result, Model model) {
        if (userService.findByUsername(request.getUsername()).isPresent()) {
            result.rejectValue("username", "error.user", "El username ya está en uso");
        }

        if (userService.existsByTelefono(request.getTelefono())) {
            result.rejectValue("telefono", "error.user", "El teléfono ya está en uso");
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty() 
                && userService.existsByEmail(request.getEmail())) {
            result.rejectValue("email", "error.user", "El email ya está en uso");
        }

        if (request.getPassword() != null && !request.getPassword().equals(confirmPassword)) {
            result.rejectValue("password", "error.user", "Las contraseñas no coinciden");
            model.addAttribute("confirmPasswordError", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return "auth/registro";
        }

        userService.save(request);

        auditService.log("REGISTRO_USUARIO", "Nuevo usuario registrado: " + request.getUsername());

        return "redirect:/login?registered";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }
}