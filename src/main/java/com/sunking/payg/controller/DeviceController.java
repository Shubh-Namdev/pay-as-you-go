package com.sunking.payg.controller;

import com.sunking.payg.dto.AssignDeviceRequest;
import com.sunking.payg.dto.CreateDeviceRequest;
import com.sunking.payg.dto.DeviceStatusResponse;
import com.sunking.payg.service.assignment.AssignmentService;
import com.sunking.payg.service.device.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final AssignmentService assignmentService;

    @PostMapping
    public void registerDevice(@RequestBody CreateDeviceRequest request) {
        deviceService.registerDevice(request);
    }

    @PostMapping("/{deviceId}/assign")
    public void assignDevice(
            @PathVariable Long deviceId,
            @RequestBody AssignDeviceRequest request
    ) {
        assignmentService.assignDevice(deviceId, request.getCustomerId());
    }

    @GetMapping("/{deviceId}/status")
    public DeviceStatusResponse getStatus(@PathVariable Long deviceId) {
        return assignmentService.getDeviceStatus(deviceId);
    }
}