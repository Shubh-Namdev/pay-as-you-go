package com.sunking.payg.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.sunking.payg.enums.Role;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}