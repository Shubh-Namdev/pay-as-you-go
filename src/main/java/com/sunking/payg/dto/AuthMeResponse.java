package com.sunking.payg.dto;

public class AuthMeResponse {
    private Long id;
    private String role;

    public AuthMeResponse(Long id, String role) {
        this.id = id;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getRole() { return role; }
}
