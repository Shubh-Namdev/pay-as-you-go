package com.sunking.payg.service.scheduler;

import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.enums.DeviceStatus;
import com.sunking.payg.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeviceScheduler {

    private final AssignmentRepository assignmentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // Runs every 1 minute (for demo, in prod → maybe 10 min)
    @Scheduled(fixedRate = 60000)
    public void lockOverdueDevices() {
        System.out.println("Schedule is checking for overdue payments");

        List<DeviceAssignment> overdueAssignments =
                assignmentRepository.findByNextDueDateBefore(LocalDateTime.now());
        
        if (overdueAssignments.isEmpty())
            System.out.println("no overdue payments");

        for (DeviceAssignment assignment : overdueAssignments) {

            if (assignment.getNextDueDate() != null &&
                assignment.getNextDueDate().isBefore(LocalDateTime.now()) &&
                assignment.getStatus() != DeviceStatus.LOCKED) {

                assignment.setStatus(DeviceStatus.LOCKED);
                assignmentRepository.save(assignment);

                // ✅ Update Redis cache
                String key = "device:" + assignment.getDeviceId() + ":status";
                redisTemplate.opsForValue().set(key, DeviceStatus.LOCKED.name());

                System.out.println("device is locked now");
            }else{
                System.out.println("device is already locked");
            }
        }
    }
}