package com.josepinodev.appdomirest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuditService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT_LOGGER");

    public void log(String accion, String detalle) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = "ANONIMO";
        String roles = "";

        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
        }

        auditLog.info("[USUARIO:{}] [ROL:{}] [ACCION:{}] {}", username, roles, accion, detalle);
    }
}
