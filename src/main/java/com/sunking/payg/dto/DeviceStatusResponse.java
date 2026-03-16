package com.sunking.payg.dto;

import com.sunking.payg.enums.DeviceStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceStatusResponse {
    private Long deviceId;
    private DeviceStatus status;
    private String message;
}