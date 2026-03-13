package com.sunking.payg.repository;

import com.sunking.payg.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    
}