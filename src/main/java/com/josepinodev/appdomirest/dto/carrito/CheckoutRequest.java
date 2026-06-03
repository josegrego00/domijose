package com.josepinodev.appdomirest.dto.carrito;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    private String direccion;
    private Long rutaId;
    private Boolean esDomicilio = true;

    private String metodoPago;
    private BigDecimal efectivoRecibido;
    private Long cuentaBancoId;
}
