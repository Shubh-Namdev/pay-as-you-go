package com.sunking.payg.controller;

import com.sunking.payg.dto.AssignDeviceRequest;
import com.sunking.payg.dto.CreateDeviceRequest;
import com.sunking.payg.dto.DeviceResponse;
import com.sunking.payg.dto.DeviceStatusResponse;
import com.sunking.payg.service.assignment.AssignmentService;
import com.sunking.payg.service.device.DeviceService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final AssignmentService assignmentService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public void registerDevice(@RequestBody CreateDeviceRequest request) {
        deviceService.registerDevice(request);
    }

    @PostMapping("/{deviceId}/assign")
    public DeviceStatusResponse assignDevice(
            @PathVariable Long deviceId,
            @RequestBody AssignDeviceRequest request
    ) {
        return assignmentService.assignDevice(deviceId, request.getCustomerId());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{deviceId}/status/me")
    public DeviceStatusResponse getStatus(@PathVariable Long deviceId, Authentication authentication) {
        Long customerId = Long.valueOf(authentication.getName());
        return assignmentService.validateDeviceAndReturnStatus(customerId, deviceId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{deviceId}/status")
    public DeviceStatusResponse getStatus(@PathVariable Long deviceId) {
        return assignmentService.getDeviceStatus(deviceId);
    }

    @GetMapping
    public Page<DeviceResponse> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return deviceService.getAllDevices(page, size);
    }


}