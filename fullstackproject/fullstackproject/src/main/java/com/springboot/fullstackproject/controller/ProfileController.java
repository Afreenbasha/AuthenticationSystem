package com.springboot.fullstackproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.fullstackproject.io.ProfileRequest;
import com.springboot.fullstackproject.io.ProfileResponse;
import com.springboot.fullstackproject.service.EmailService;
import com.springboot.fullstackproject.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController

@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "true")

public class ProfileController {
	
	private final ProfileService profileService;
	  private final EmailService emailService;
	
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
		ProfileResponse response=profileService.createProfile(request);
		//TODO : send welcome email
		emailService.sendWelcomeEmail(response.getEmail(), response.getName());
		return response;
	}
	
	@GetMapping("/profile")
	public ProfileResponse getProfile(@CurrentSecurityContext(expression="authentication?.name")String email) {
		return profileService.getProfile(email);
	}

}

