package com.domicilio.domijose.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para items del carrito de compras
 * Este DTO es solo para la vista, NO es una entidad JPA
 * Sigue la regla: Un DTO por funcionalidad específica
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemDTO {
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    
    // Constructor específico para crear desde producto
    public CarritoItemDTO(Long productId, String productName, String productImage, BigDecimal price, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Método para recalcular subtotal cuando cambia cantidad o precio
    public void recalcularSubtotal() {
        if (price != null && quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    // Sobrescribir setQuantity para recalcular automáticamente
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        recalcularSubtotal();
    }
    
    // Sobrescribir setPrice para recalcular automáticamente
    public void setPrice(BigDecimal price) {
        this.price = price;
        recalcularSubtotal();
    }
}