package com.sunking.payg.repository;

import com.sunking.payg.entity.Device;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;




public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    Optional<Device> findById(Long id);
    Page<Device> findByAvailable(boolean available, Pageable pageable);
}