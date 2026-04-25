package com.domicilio.domijose.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "metodos_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String banco;
    
    @Column(name = "tipo_cuenta")
    private String tipoCuenta;
    
    @Column(name = "numero_cuenta")
    private String numeroCuenta;
    
    @Column(name = "nombre_titular")
    private String nombreTitular;
    
    @Column(name = "qr_url")
    private String qrUrl;
    
    private boolean activo = true;
}