package com.sunking.payg.service.device;

import org.springframework.data.domain.Page;

import com.sunking.payg.dto.CreateDeviceRequest;
import com.sunking.payg.dto.DeviceResponse;

public interface DeviceService {
    void registerDevice(CreateDeviceRequest request);
    Page<DeviceResponse> getAllDevices(int page,int size);
}