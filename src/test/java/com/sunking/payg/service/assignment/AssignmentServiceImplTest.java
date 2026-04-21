package com.sunking.payg.service.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.sunking.payg.dto.DeviceStatusResponse;
import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.enums.DeviceStatus;
import com.sunking.payg.repository.AssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import com.sunking.payg.repository.UserRepository;


@ExtendWith(MockitoExtension.class)
public class AssignmentServiceImplTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;


    @Test
    void shouldReturnDeviceStatusFromCache_whenCacheHit() {
        Long deviceId = 1L;
        String key = "device:" + deviceId + ":status";
   
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("ACTIVE");

        // Act
        DeviceStatusResponse deviceStatus = assignmentService.getDeviceStatus(deviceId);

        // Assert
        assertEquals(deviceId, deviceStatus.getDeviceId());
        assertTrue(deviceStatus.getMessage().contains("Fetched from cache"));

        verify(assignmentRepository, never())
            .findByDeviceId(deviceId);
    }
 
    @Test
    void shouldReturnDeviceStatusFromDB_andCacheIt_whenCacheMiss() {
        Long deviceId = 1L;
        String key = "device:" + deviceId + ":status";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        DeviceAssignment deviceAssignment = new DeviceAssignment();
        deviceAssignment.setId(1L);
        deviceAssignment.setStatus(DeviceStatus.ACTIVE);

        when(assignmentRepository.findByDeviceId(deviceId))
            .thenReturn(Optional.of(deviceAssignment));
        doNothing().when(valueOperations).set(key, deviceAssignment.getStatus().name());

        // Act
        DeviceStatusResponse deviceStatusResponse = assignmentService.getDeviceStatus(deviceId);

        // Assert
        assertEquals(DeviceStatus.ACTIVE, deviceStatusResponse.getStatus().name());

        verify(valueOperations, times(1))
            .get(key);
        verify(assignmentRepository, times(1))
            .findByDeviceId(deviceId);
        verify(valueOperations, times(1))
            .set(key, deviceAssignment.getStatus().name());
    }
}
