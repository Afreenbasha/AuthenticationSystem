package com.springboot.fullstackproject.io;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
     private String email;
     private String token;
     
}
