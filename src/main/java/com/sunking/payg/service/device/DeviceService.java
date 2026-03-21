package com.sunking.payg.service.device;

import com.sunking.payg.dto.CreateDeviceRequest;

public interface DeviceService {
    void registerDevice(CreateDeviceRequest request);
}