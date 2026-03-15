package com.sunking.payg.service.assignment;

import com.sunking.payg.dto.DeviceStatusResponse;

public interface AssignmentService {

    void assignDevice(Long deviceId, Long customerId);

    DeviceStatusResponse getDeviceStatus(Long deviceId);
}