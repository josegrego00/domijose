package com.josepinodev.appdomirest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {
    private String tipo;
    private String mensaje;
    private Long ventaId;
    private BigDecimal total;
    private String usuarioNombre;
    private LocalDateTime timestamp;
    private String estado;
}
