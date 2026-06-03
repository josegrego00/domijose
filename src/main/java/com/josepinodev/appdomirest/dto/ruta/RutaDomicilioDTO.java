package com.josepinodev.appdomirest.dto.ruta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaDomicilioDTO {

    private Long id;

    @NotBlank(message = "El barrio es obligatorio")
    private String barrio;

    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    private Boolean activo = true;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}