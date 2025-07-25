package com.springboot.fullstackproject.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.fullstackproject.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity ,Long>{
    Optional<UserEntity>findByEmail(String email);
    Boolean existsByEmail(String email);
    
    
}