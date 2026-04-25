package com.domicilio.domijose.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.domicilio.domijose.models.enums.OrderStatus;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDIENTE;
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
    }
    
    // Métodos de negocio
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        calculateTotal();
    }
    
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        calculateTotal();
    }
    
    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public boolean canBeCancelled() {
        return status == OrderStatus.POR_CONFIRMAR;
    }
    
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Solo se pueden cancelar pedidos por confirmar");
        }
        this.status = OrderStatus.CANCELADO;
    }
    
    public void deliver() {
        if (status != OrderStatus.EN_CAMINO) {
            throw new IllegalStateException("Solo se pueden entregar pedidos en camino");
        }
        this.status = OrderStatus.ENTREGADO;
    }
}