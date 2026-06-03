package com.josepinodev.appdomirest.dto.producto;

import com.josepinodev.appdomirest.model.ECategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    private boolean disponible = true;

    @PositiveOrZero(message = "El precio de compra no puede ser negativo")
    private BigDecimal precioCompra;

    @PositiveOrZero(message = "El precio de venta no puede ser negativo")
    private BigDecimal precioVenta;

    private Boolean activo = true;

    @NotBlank(message = "El codigo es obligatorio")
    private String codigo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private Long usuarioCreadorId;

    private String imagenUrl;

    private Boolean enPromocion = false;

    private BigDecimal precioPromocional;

    private ECategoria categoria;

}