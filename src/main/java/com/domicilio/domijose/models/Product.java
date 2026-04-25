package com.domicilio.domijose.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    private String imageUrl;

    private boolean available = true;

    private String category;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Método de negocio: validar stock disponible
    public boolean hasStock(int quantity) {
        return stock >= quantity;
    }
    
    // Método de negocio: reducir stock
    public void reduceStock(int quantity) {
        if (hasStock(quantity)) {
            this.stock -= quantity;
        } else {
            throw new IllegalArgumentException("Stock insuficiente");
        }
    }
    
    // Método de negocio: aumentar stock
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}