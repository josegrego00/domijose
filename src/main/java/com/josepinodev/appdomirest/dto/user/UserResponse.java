package com.josepinodev.appdomirest.dto.user;

import com.josepinodev.appdomirest.dto.role.RoleDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String password;
    private String telefono;
    private String email;
    private String direccion;
    private List<String> direcciones;
    private Set<RoleDTO> roles;
    private LocalDateTime fechaCreacion;
    private Boolean activo;
}