package com.sunking.payg.service.assignment;

import com.sunking.payg.dto.DeviceStatusResponse;
import com.sunking.payg.entity.Customer;
import com.sunking.payg.entity.Device;
import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.enums.DeviceStatus;
import com.sunking.payg.repository.AssignmentRepository;
import com.sunking.payg.repository.CustomerRepository;
import com.sunking.payg.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CustomerRepository customerRepository;
    private final DeviceRepository deviceRepository;

    @Override
    public void assignDevice(Long deviceId, Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        DeviceAssignment assignment = new DeviceAssignment();
        assignment.setCustomerId(customer.getId());
        assignment.setDeviceId(device.getId());

        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus(DeviceStatus.ACTIVE);

        assignment.setRemainingBalance(device.getTotalCost());
        assignment.setLastPaymentDate(LocalDateTime.now());

        // next due calculation
        if (device.getPaymentPlanType().name().equals("DAILY")) {
            assignment.setNextDueDate(LocalDateTime.now().plusDays(1));
        } else {
            assignment.setNextDueDate(LocalDateTime.now().plusWeeks(1));
        }

        assignmentRepository.save(assignment);
    }

    @Override
    public DeviceStatusResponse getDeviceStatus(Long deviceId) {

        DeviceAssignment assignment = assignmentRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        return DeviceStatusResponse.builder()
                .deviceId(deviceId)
                .status(assignment.getStatus())
                .message("Device status fetched successfully")
                .build();
    }
}