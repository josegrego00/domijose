package com.josepinodev.appdomirest.dto.carrito;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoDTO {

    private List<ItemCarritoDTO> items = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal cantidadItems = BigDecimal.ZERO;

    public void addItem(ItemCarritoDTO item) {
        this.items.add(item);
        recalcularTotal();
    }

    public void removeItem(Long productoId) {
        this.items.removeIf(item -> item.getProductoId().equals(productoId));
        recalcularTotal();
    }

    public void updateCantidad(Long productoId, BigDecimal cantidad) {
        this.items.forEach(item -> {
            if (item.getProductoId().equals(productoId)) {
                item.setCantidad(cantidad);
            }
        });
        recalcularTotal();
    }

    public void recalcularTotal() {
        this.total = items.stream()
                .map(ItemCarritoDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.cantidadItems = items.stream()
                .map(ItemCarritoDTO::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clear() {
        this.items.clear();
        this.total = BigDecimal.ZERO;
        this.cantidadItems = BigDecimal.ZERO;
    }

    public ItemCarritoDTO findByProductoIdAndObservacion(Long productoId, String observacion) {
        return items.stream()
                .filter(item -> item.getProductoId().equals(productoId)
                        && (item.getObservacion() == null ? observacion == null : item.getObservacion().equals(observacion)))
                .findFirst()
                .orElse(null);
    }
}