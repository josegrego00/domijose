package com.josepinodev.appdomirest.dto.role;

import com.josepinodev.appdomirest.model.ERole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;

    private ERole name;
}