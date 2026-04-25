package com.domicilio.domijose.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoDTO {
    private Long id;
    private String banco;
    private String tipoCuenta;
    private String numeroCuenta;
    private String nombreTitular;
    private String qrUrl;
    private boolean activo;
    private MultipartFile qrFile;
}