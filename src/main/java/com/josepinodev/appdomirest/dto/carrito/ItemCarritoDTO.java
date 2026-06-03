package com.josepinodev.appdomirest.dto.carrito;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoDTO {

    private Long productoId;
    private String nombre;
    private BigDecimal precio;
    private String imagenUrl;
    private BigDecimal cantidad;
    private BigDecimal subtotal;
    private String observacion;

    public ItemCarritoDTO(Long productoId, String nombre, BigDecimal precio, String imagenUrl, BigDecimal cantidad) {
        this(productoId, nombre, precio, imagenUrl, cantidad, null);
    }

    public ItemCarritoDTO(Long productoId, String nombre, BigDecimal precio, String imagenUrl, BigDecimal cantidad, String observacion) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.cantidad = cantidad;
        this.observacion = observacion;
        this.subtotal = precio.multiply(cantidad);
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        this.subtotal = precio.multiply(cantidad);
    }
}