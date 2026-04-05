package com.sunking.payg.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunking.payg.dto.AuthMeResponse;
import com.sunking.payg.dto.LoginRequest;
import com.sunking.payg.entity.User;
import com.sunking.payg.exceptions.BusinessException;
import com.sunking.payg.exceptions.ResourceNotFoundException;
import com.sunking.payg.jwt.JwtUtil;
import com.sunking.payg.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        User user = userRepository
                .findByPhone(request.getPhone())
                .orElseThrow(() -> new ResourceNotFoundException("user not fonund with phone "+ request.getPhone()));

        // password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("invalid credentials");
        }

        // generate token
        return jwtUtil.generateToken(String.valueOf(user.getId()), user.getRole().name());
    }


    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> getCurrentUser(Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());

        // Get role from authorities
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("UNKNOWN");
        String trimmedRol = role.replace("ROLE_","");
        AuthMeResponse response = new AuthMeResponse(userId, trimmedRol);

        return ResponseEntity.ok(response);
    }
}
