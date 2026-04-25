package com.domicilio.domijose.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class LoggingControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger(LoggingControllerAdvice.class);

    @ModelAttribute
    public void logRequest(HttpServletRequest request, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String userPhone = auth.getName();  // ✅ Cambiado: ahora es phone, no email
            String role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("SIN_ROL");
            log.info("Usuario en sesión: {} | Rol: {} | IP: {} | URI: {}", 
                    userPhone, role, request.getRemoteAddr(), request.getRequestURI());
        } else {
            log.debug("Request sin sesión autenticada: {} | IP: {}", 
                    request.getRequestURI(), request.getRemoteAddr());
        }
    }
}