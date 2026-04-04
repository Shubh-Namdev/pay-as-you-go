


package com.sunking.payg.service.user;

import com.sunking.payg.dto.CreateUserRequest;
import com.sunking.payg.dto.UserResponse;
import com.sunking.payg.entity.User;
import com.sunking.payg.enums.Role;
import com.sunking.payg.exceptions.BusinessException;
import com.sunking.payg.exceptions.DuplicateResourceException;
import com.sunking.payg.exceptions.ResourceNotFoundException;
import com.sunking.payg.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        log.info("Creating user with phone={}", request.getPhone());

        // ✅ Duplicate check
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("Duplicate user creation attempt for phone={}", request.getPhone());
            throw new DuplicateResourceException("User already exists with phone: " + request.getPhone());
        }

        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setRole(Role.CUSTOMER);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);

        log.info("User created successfully with userId={}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    public UserResponse getUser(Long id) {

        log.info("Fetching user with userId={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with userId={}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });

        return mapToResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size) {

        log.info("Fetching users with page={}, size={}", page, size);

        if (page < 0 || size <= 0) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            throw new BusinessException("Invalid pagination parameters");
        }

        Page<UserResponse> result = userRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);

        log.info("Fetched {} users for page={}", result.getNumberOfElements(), page);

        return result;
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .build();
    }
}



























// package com.sunking.payg.service.user;

// import com.sunking.payg.dto.CreateUserRequest;
// import com.sunking.payg.dto.UserResponse;
// import com.sunking.payg.entity.User;
// import com.sunking.payg.enums.Role;
// import com.sunking.payg.exceptions.BusinessException;
// import com.sunking.payg.exceptions.DuplicateResourceException;
// import com.sunking.payg.exceptions.ResourceNotFoundException;
// import com.sunking.payg.repository.UserRepository;
// import lombok.RequiredArgsConstructor;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;

// @Service
// @RequiredArgsConstructor
// public class UserServiceImpl implements UserService {

//     private final PasswordEncoder passwordEncoder;
//     private final UserRepository userRepository;

//     @Override
//     public UserResponse createUser(CreateUserRequest request) {

//         // duplicacy check
//         if ( userRepository.existsByPhone(request.getPhone()) )
//             throw new DuplicateResourceException("User already exists with phone: " + request.getPhone());


//         User user = new User();
//         user.setName(request.getName());
//         user.setPhone(request.getPhone());
//         user.setRole(Role.CUSTOMER);
//         user.setPassword(passwordEncoder.encode(request.getPassword()));

//         User saved = userRepository.save(user);

//         return mapToResponse(saved);
//     }

//     @Override
//     public UserResponse getUser(Long id) {
//         User user = userRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

//         return mapToResponse(user);
//     }

//     @Override
//     public Page<UserResponse> getAllUsers(int page, int size) {
//         if (page < 0 || size <= 0)
//             throw new BusinessException("Invalid pagination parameters");

//         return userRepository.findAll(PageRequest.of(page, size))
//                 .map(this::mapToResponse);
//     }

//     private UserResponse mapToResponse(User user) {
//         return UserResponse.builder()
//                 .id(user.getId())
//                 .name(user.getName())
//                 .phone(user.getPhone())
//                 .build();
//     }
// }