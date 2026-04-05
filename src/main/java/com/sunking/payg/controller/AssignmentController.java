package com.sunking.payg.controller;

import com.sunking.payg.dto.AssignDeviceRequest;
import com.sunking.payg.dto.DeviceStatusResponse;
import com.sunking.payg.service.assignment.AssignmentService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/devices/assign")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{deviceId}")
    public DeviceStatusResponse adminAssignDevice(
            @PathVariable Long deviceId,
            @RequestBody AssignDeviceRequest request
    ) {
        return assignmentService.assignDevice(deviceId, request.getCustomerId());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{deviceId}/me")
    public DeviceStatusResponse customerAssignDevice(
            @PathVariable Long deviceId,
            Authentication authentication
    ) {
        Long customerId = Long.valueOf(authentication.getName());
        return assignmentService.assignDevice(deviceId, customerId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public Page<DeviceStatusResponse> getCustomerAssignedDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
        ) {

        Long customerId = Long.valueOf(authentication.getName());
        return assignmentService.getAllCustomerAssignedDevices(customerId, page, size);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{deviceId}/status")
    public DeviceStatusResponse getStatus(@PathVariable Long deviceId) {
        return assignmentService.getDeviceStatus(deviceId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Page<DeviceStatusResponse> getAllAssignedDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {

        return assignmentService.getAllAssignedDevices(page, size);
    }

}