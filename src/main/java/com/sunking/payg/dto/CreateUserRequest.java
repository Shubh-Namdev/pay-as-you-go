package com.sunking.payg.dto;


import jakarta.persistence.Column;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;
}
