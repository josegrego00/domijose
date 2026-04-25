package com.domicilio.domijose.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Teléfono inválido")
    private String phone;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String fullName;
    
    
    private String email;
    
    private String role; // Solo para response (mostrar en vistas)
    
    // Método útil para saber si es nuevo registro
    public boolean isNew() {
        return id == null;
    }
}