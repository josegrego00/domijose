package com.domicilio.domijose.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.domicilio.domijose.models.enums.OrderStatus;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    OrderStatus status;

    BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItem> items;
}