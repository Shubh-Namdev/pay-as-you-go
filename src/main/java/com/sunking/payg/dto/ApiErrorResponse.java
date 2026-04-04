package com.sunking.payg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    
    private int status;
    private String message;
    private String error;
    private long timestamp;
}
