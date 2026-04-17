package com.domicilio.domijose.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Integer quantity;

    BigDecimal unitPrice;

    BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Product product;
}