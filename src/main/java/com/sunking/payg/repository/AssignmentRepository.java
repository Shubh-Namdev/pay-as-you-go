package com.sunking.payg.repository;

import com.sunking.payg.entity.DeviceAssignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<DeviceAssignment, Long> {

    Optional<DeviceAssignment> findByDeviceId(Long deviceId);
    List<DeviceAssignment> findByNextDueDateBefore(LocalDateTime time);
    Page<DeviceAssignment> findByCustomerId(Long customerId, Pageable pageable);
}