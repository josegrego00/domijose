package com.domicilio.domijose.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.domicilio.domijose.models.enums.Role;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String phone;  // ← Este es el username para login

    
    private String email;  // ← Ahora es opcional (puede ser null)

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;

    // Constructor útil para registro
    public User(String phone, String password, String fullName, Role role) {
        this.phone = phone;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }
}