package com.sunking.payg.service.device;

import com.sunking.payg.dto.CreateDeviceRequest;
import com.sunking.payg.entity.Device;
import com.sunking.payg.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
}