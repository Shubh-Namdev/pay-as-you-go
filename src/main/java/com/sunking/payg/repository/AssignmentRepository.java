package com.sunking.payg.repository;

import com.sunking.payg.entity.DeviceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<DeviceAssignment, Long> {

    Optional<DeviceAssignment> findByDeviceId(Long deviceId);
}