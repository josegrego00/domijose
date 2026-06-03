package com.josepinodev.appdomirest.dto.user;

import com.josepinodev.appdomirest.dto.role.RoleDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private Long id;

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "La contrasenia es obligatoria")
    @Size(min = 6, message = "La contrasenia debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El telefono es obligatorio")
    @Size(min = 8, max = 20, message = "El telefono debe tener entre 8 y 20 digitos")
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser valido")
    private String email;

    private String direccion;

    private Set<RoleDTO> roles;
}