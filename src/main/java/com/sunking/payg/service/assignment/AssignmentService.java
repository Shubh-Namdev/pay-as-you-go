package com.sunking.payg.service.assignment;

import org.springframework.data.domain.Page;

import com.sunking.payg.dto.DeviceStatusResponse;

public interface AssignmentService {

    DeviceStatusResponse assignDevice(Long deviceId, Long customerId);
    DeviceStatusResponse getDeviceStatus(Long deviceId);
    public Page<DeviceStatusResponse> getAllCustomerAssignedDevices(Long customerId, int page, int size);
    public Page<DeviceStatusResponse> getAllAssignedDevices(int page, int size);
}