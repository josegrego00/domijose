package com.josepinodev.appdomirest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "productos")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "disponible")
    private boolean disponible = true;

    private BigDecimal precioCompra;

    private BigDecimal precioVenta;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "en_promocion")
    private Boolean enPromocion = false;

    @Column(name = "precio_promocional")
    private BigDecimal precioPromocional;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "codigo", unique = true)
    private String codigo;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "usuario_creador_id")
    private UserEntity usuarioCreador;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private ECategoria categoria;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
        if (enPromocion == null) {
            enPromocion = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}