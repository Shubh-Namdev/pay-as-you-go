package com.sunking.payg.service.user;

import com.sunking.payg.dto.CreateUserRequest;
import com.sunking.payg.dto.UserResponse;
import org.springframework.data.domain.Page;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUser(Long id);

    Page<UserResponse> getAllUsers(int page, int size);
}