package com.sunking.payg.service.customer;

import com.sunking.payg.dto.CreateCustomerRequest;
import com.sunking.payg.dto.CustomerResponse;
import org.springframework.data.domain.Page;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    CustomerResponse getCustomer(Long id);

    Page<CustomerResponse> getAllCustomers(int page, int size);
}