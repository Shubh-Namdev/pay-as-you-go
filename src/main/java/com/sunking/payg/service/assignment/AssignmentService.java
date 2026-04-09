package com.sunking.payg.service.assignment;

import com.sunking.payg.dto.DeviceStatusResponse;

public interface AssignmentService {

    DeviceStatusResponse assignDevice(Long deviceId, Long customerId);
    DeviceStatusResponse getDeviceStatus(Long deviceId);
    DeviceStatusResponse validateDeviceAndReturnStatus(Long customerId, Long deviceId);
}