package com.domicilio.domijose.dto;

import com.domicilio.domijose.models.enums.OrderStatus;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private LocalDateTime orderDate;
    private OrderStatus status;
    
    @Positive(message = "El total debe ser mayor a 0")
    private BigDecimal totalAmount;
    
    private Long userId;
    private String userPhone;
    private String userFullName;
    private String metodoPago;
    
    private List<OrderItemDTO> items;
    
    public boolean isCancelable() {
        return status == OrderStatus.PENDIENTE;
    }
}