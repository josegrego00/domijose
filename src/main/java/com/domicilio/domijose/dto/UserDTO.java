package com.domicilio.domijose.dto;

import com.domicilio.domijose.models.enums.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;

    private String email;

    private String fullName;
    private String phone;
    private Role role;

    public UserDTO(Long id, String email, String fullName, String phone, Role role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }

}