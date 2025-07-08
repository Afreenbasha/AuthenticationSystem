package com.springboot.fullstackproject.service;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.springboot.fullstackproject.entity.UserEntity;
import com.springboot.fullstackproject.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity existingUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Email not Found: " + email));
        
        // Spring Security's built-in User class
        return new User(
            existingUser.getEmail(), 
            existingUser.getPassword(), 
            new ArrayList<>() // You can replace this with authorities if available
        );
    }
}

