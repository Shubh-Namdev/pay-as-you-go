package com.sunking.payg.service.assignment;

import com.sunking.payg.dto.DeviceStatusResponse;
import com.sunking.payg.entity.User;
import com.sunking.payg.entity.Device;
import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.enums.DeviceStatus;
import com.sunking.payg.exceptions.BusinessException;
import com.sunking.payg.exceptions.ResourceNotFoundException;
import com.sunking.payg.repository.AssignmentRepository;
import com.sunking.payg.repository.UserRepository;
import com.sunking.payg.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository customerRepository;
    private final DeviceRepository deviceRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public DeviceStatusResponse assignDevice(Long deviceId, Long customerId) {
        log.info("Assigning deviceId={} to customerId={}", deviceId, customerId);

        // validate customer
        User customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found for customerId={}", customerId);
                    return new ResourceNotFoundException("Customer not found with id: " + customerId);
                });

        // validate device
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> {
                    log.warn("Device not found for deviceId={}", deviceId);
                    return new ResourceNotFoundException("Device not found with id: " + deviceId);
                });

        // already assigned
        assignmentRepository.findByDeviceId(deviceId)
                .ifPresent(a -> {
                    log.warn("Device already assigned: deviceId={}", deviceId);
                    throw new BusinessException("Device already assigned");
                });

        // assign device
        DeviceAssignment assignment = new DeviceAssignment();
        assignment.setCustomerId(customer.getId());
        assignment.setDeviceId(device.getId());

        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus(DeviceStatus.ACTIVE);

        assignment.setRemainingBalance(device.getTotalCost());
        assignment.setLastPaymentDate(LocalDateTime.now());

        // next due calculation
        if (device.getPaymentPlanType().name().equals("DAILY")) {          
            assignment.setNextDueDate(LocalDateTime.now().plusMinutes(5));
            // assignment.setNextDueDate(LocalDateTime.now().plusDays(1));

        } else {
            assignment.setNextDueDate(LocalDateTime.now().plusWeeks(1));
        }

        assignmentRepository.save(assignment);
        log.info("Device assigned successfully: deviceId={}, customerId={}", deviceId, customerId);

        redisTemplate.opsForValue().set(
                "device:" + deviceId + ":customerId",
                customerId
        );

        redisTemplate.opsForValue().set(
                "device:" + deviceId + ":status",
                DeviceStatus.ACTIVE.name()
        );

        return mapToResponse(assignment);
    }

    
    @Override
    public DeviceStatusResponse getDeviceStatus(Long deviceId) {
        String key = "device:" + deviceId + ":status";

        // Check cache
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.debug("Cache hit for device status: deviceId={}", deviceId);
            return DeviceStatusResponse.builder()
                    .deviceId(deviceId)
                    .status(DeviceStatus.valueOf(cached.toString()))
                    .message("Fetched from cache")
                    .build();
        }
        log.debug("Cache miss for device status: deviceId={}", deviceId);

        // fetch from db
       DeviceAssignment assignment = assignmentRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> {
                    log.warn("Assignment not found for deviceId={}", deviceId);
                    return new ResourceNotFoundException("Assignment not found for device: " + deviceId);
                });                       

        // cache it
        redisTemplate.opsForValue().set(key, assignment.getStatus().name());

        return mapToResponse(assignment);
    }


    public DeviceStatusResponse validateDeviceAndReturnStatus(Long customerId, Long deviceId) {

        String key = "device:" + deviceId + ":customerId";

        // check cache
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            Long cachedCustomerId = Long.valueOf(cached.toString());

            if (!cachedCustomerId.equals(customerId))
                throw new BusinessException("Invalid device access");

            return getDeviceStatus(deviceId);
        }

        // fetch from db
        DeviceAssignment assignment = assignmentRepository.findByDeviceId(deviceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Assignment not found for device: " + deviceId));

        if (!assignment.getCustomerId().equals(customerId)) 
            throw new BusinessException("Invalid device access");
        
        // cache it
        redisTemplate.opsForValue().set(key, customerId);

        return mapToResponse(assignment);
    }

    
    private DeviceStatusResponse mapToResponse(DeviceAssignment assignment) {

        return DeviceStatusResponse.builder()
                .deviceId(assignment.getDeviceId())
                .status(assignment.getStatus())
                .assignedAt(assignment.getAssignedAt())
                .lastPaymentDate(assignment.getLastPaymentDate())
                .nextDueDate(assignment.getNextDueDate())
                .remainingBalance(assignment.getRemainingBalance())
                .message("SUCCESS")
                .build();
    }

}