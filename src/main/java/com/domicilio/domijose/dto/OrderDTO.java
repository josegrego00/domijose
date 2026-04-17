package com.domicilio.domijose.dto;

import com.domicilio.domijose.models.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class OrderDTO {
    private Long id;

    private LocalDateTime orderDate;

    private OrderStatus status;

    @NotNull
    @Positive
    private BigDecimal totalAmount;

    private List<OrderItemDTO> items;

    @Data
    @NoArgsConstructor
    public static class OrderItemDTO {
        private Long id;
        private String productName;
        private String productImageUrl;
        
        @NotNull
        @Positive
        private Integer quantity;
        
        @NotNull
        @Positive
        private BigDecimal unitPrice;
        
        @NotNull
        @Positive
        private BigDecimal subtotal;
    }
}