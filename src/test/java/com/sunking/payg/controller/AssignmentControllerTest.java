package com.sunking.payg.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunking.payg.dto.AssignDeviceRequest;
import com.sunking.payg.dto.DeviceStatusResponse;
import com.sunking.payg.enums.DeviceStatus;
import com.sunking.payg.service.assignment.AssignmentService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AssignmentController.class)
public class AssignmentControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssignmentService assignmentService;


    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void adminShouldAssignDevice() throws Exception {

        AssignDeviceRequest assignDeviceRequest = new AssignDeviceRequest();
        assignDeviceRequest.setCustomerId(1L);

        Long deviceId = 1L;

        DeviceStatusResponse deviceStatusResponse = DeviceStatusResponse.builder()
                                                        .deviceId(deviceId)
                                                        .status(DeviceStatus.ACTIVE)
                                                        .lastPaymentDate(LocalDateTime.now())
                                                        .remainingBalance(BigDecimal.valueOf(1000))
                                                        .build();


        when(assignmentService.assignDevice(deviceId, assignDeviceRequest.getCustomerId()))
            .thenReturn(deviceStatusResponse);


        mockMvc.perform(
            post("/devices/assign/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(assignDeviceRequest))
        )
        .andExpect(status().isOk());


        verify(assignmentService, times(1))
               .assignDevice(deviceId, assignDeviceRequest.getCustomerId());
    }
}
