package com.sunking.payg.service.device;

import com.sunking.payg.dto.CreateDeviceRequest;
import com.sunking.payg.dto.DeviceResponse;
import com.sunking.payg.entity.Device;
import com.sunking.payg.exceptions.BusinessException;
import com.sunking.payg.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    public void registerDevice(CreateDeviceRequest request) {
        Device device = new Device();
        device.setSerialNumber(request.getSerialNumber());
        device.setTotalCost(request.getTotalCost());
        device.setPaymentPlanType(request.getPaymentPlanType());
        device.setPaymentAmount(request.getPaymentAmount());

        deviceRepository.save(device);
    }

    @Override
    public Page<DeviceResponse> getAllDevices(int page, int size) {

        log.info("Fetching devices with page={}, size={}", page, size);

        if (page < 0 || size <= 0) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            throw new BusinessException("Invalid pagination parameters");
        }

        Page<DeviceResponse> result = deviceRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);

        log.info("Fetched {} devices for page={}", result.getNumberOfElements(), page);

        return result;
    }

    private DeviceResponse mapToResponse(Device device) {
        return DeviceResponse.builder()
                   .id(device.getId())
                   .serialNumber(device.getSerialNumber())
                   .totalCost(device.getTotalCost())
                   .paymentPlanType(device.getPaymentPlanType())
                   .paymentAmount(device.getPaymentAmount())
                   .imageUrl(device.getImageUrl())
                   .build();
    }
}