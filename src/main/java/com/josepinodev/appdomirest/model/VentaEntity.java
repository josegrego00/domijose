package com.josepinodev.appdomirest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ventas")
public class VentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserEntity usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EVentaEstado estado = EVentaEstado.PENDIENTE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id")
    private RutaDomicilioEntity rutaDomicilio;

    private BigDecimal costoDomicilio;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVentaEntity> detalles = new ArrayList<>();

    @Column(nullable = false)
    private Boolean esDomicilio = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private EMetodoPago metodoPago;

    @Column(name = "efectivo_recibido", precision = 10, scale = 2)
    private BigDecimal efectivoRecibido;

    @Column(name = "efectivo_cambio", precision = 10, scale = 2)
    private BigDecimal efectivoCambio;

    @Column(name = "monto_transferencia", precision = 10, scale = 2)
    private BigDecimal montoTransferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_banco_id")
    private CuentaBancoEntity cuentaBanco;

    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }
}