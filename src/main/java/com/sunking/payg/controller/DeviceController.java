package com.sunking.payg.controller;

import com.sunking.payg.dto.CreateDeviceRequest;
import com.sunking.payg.dto.DeviceResponse;
import com.sunking.payg.service.device.DeviceService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public void registerDevice(@RequestBody CreateDeviceRequest request) {
        deviceService.registerDevice(request);
    }

    @GetMapping("/{deviceId}")
    public DeviceResponse getDeviceById(@PathVariable Long deviceId) {
        return deviceService.findByDeviceId(deviceId);
    }
    

    @GetMapping
    public Page<DeviceResponse> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return deviceService.getAllDevices(page, size);
    }
}