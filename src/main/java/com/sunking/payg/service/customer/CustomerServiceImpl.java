package com.sunking.payg.service.customer;

import com.sunking.payg.dto.CreateCustomerRequest;
import com.sunking.payg.dto.CustomerResponse;
import com.sunking.payg.entity.Customer;
import com.sunking.payg.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());

        Customer saved = customerRepository.save(customer);

        return mapToResponse(saved);
    }

    @Override
    public CustomerResponse getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return mapToResponse(customer);
    }

    @Override
    public Page<CustomerResponse> getAllCustomers(int page, int size) {
        return customerRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .build();
    }
}