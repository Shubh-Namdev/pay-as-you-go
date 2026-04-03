package com.sunking.payg.repository;

import com.sunking.payg.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    
    public Optional<User> findByPhone(String phone);
    public boolean existsByPhone(String phone);
}