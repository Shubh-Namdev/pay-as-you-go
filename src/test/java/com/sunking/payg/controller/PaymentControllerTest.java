package com.sunking.payg.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunking.payg.dto.CreatePaymentRequest;
import com.sunking.payg.dto.PaymentResponse;
import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.repository.PaymentRepository;
import com.sunking.payg.service.payment.PaymentService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService  paymentService;

    @MockBean
    private PaymentRepository paymentRepository;


    @Test
    @WithMockUser(username = "1",roles = {"CUSTOMER"})
    void shouldCreatePaymentSuccessfully() throws Exception {
        
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setIdempotencyKey("DHHSLKH57GHI");
        paymentRequest.setDeviceId(1L);
        paymentRequest.setAmount(BigDecimal.valueOf(100.00));

        PaymentResponse paymentResponse = PaymentResponse.builder()
                       .id(1L).customerId(1L)
                       .deviceId(paymentRequest.getDeviceId())
                       .amount(paymentRequest.getAmount())
                       .status(PaymentStatus.PENDING)
                       .build();

        when(paymentService.createPayment(any(CreatePaymentRequest.class), anyLong()))
            .thenReturn(paymentResponse);

            
        mockMvc.perform(
            post("/payments")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            // .content(objectMapper.writeValueAsString(paymentRequest))
            .content(
                """
                    {
                       "deviceId":1,
                       "amount": 100.00,
                       "idempotencyKey":"DHHSLKH57GHI"
                    }
                """    
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    
    @Test
    @WithMockUser
    void shouldHandleCallbackSuccessfully() throws Exception {

        Long paymentId = 1L;
        String status = "SUCCESS";
        String transactionId = "HGKJB57544GFH";


        Payment paymentDetails = new Payment();
        paymentDetails.setId(1L);
        paymentDetails.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId))
            .thenReturn(Optional.of(paymentDetails));

        doNothing().when(paymentService).updatePaymentStatus(paymentId, status, transactionId);


        mockMvc.perform(
            post("/payments/callback")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                    {
                       "paymentId": 1,
                       "status": "SUCCESS",
                       "transactionId": "HGKJB57544GFH"
                    }
                """
            ))
            .andExpect(status().isOk());
    }



    @Test
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    void shouldShowPaymentStatus() throws Exception{

        Long deviceId = 1L;
        Long customerId = 1L;

        PaymentResponse paymentResponse = PaymentResponse.builder()
                                              .id(1L)
                                              .status(PaymentStatus.PENDING)
                                              .build();

        when(paymentService.getPaymentStatus(customerId, deviceId))
            .thenReturn(paymentResponse);

        MockHttpServletResponse response = mockMvc.perform(
                get("/payments/device/1")
                .accept(MediaType.APPLICATION_JSON)
            )
            .andReturn()
            .getResponse();

        PaymentResponse resultResponse = objectMapper.readValue(response.getContentAsString(), PaymentResponse.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(1L, resultResponse.getId());


        verify(paymentService, times(1))
              .getPaymentStatus(customerId, deviceId);
    }
}
