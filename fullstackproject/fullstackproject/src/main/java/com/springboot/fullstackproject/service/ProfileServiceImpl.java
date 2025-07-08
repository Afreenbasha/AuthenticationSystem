package com.springboot.fullstackproject.service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.springboot.fullstackproject.entity.UserEntity;
import com.springboot.fullstackproject.io.ProfileRequest;
import com.springboot.fullstackproject.io.ProfileResponse;
import com.springboot.fullstackproject.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;

	@Override
	public ProfileResponse createProfile(ProfileRequest request) {
		UserEntity newProfile = convertToUserEntity(request);
		if (!userRepository.existsByEmail(request.getEmail())) {
			newProfile = userRepository.save(newProfile);
			return convertToProfileResponse(newProfile);
		}
		throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists!!");

	}

	private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
		return ProfileResponse.builder().userId(newProfile.getUserId()).name(newProfile.getName())
				.email(newProfile.getEmail()).isAccountVerified(newProfile.getIsAccountVerified()).build();
	}

	private UserEntity convertToUserEntity(ProfileRequest request) {
		return UserEntity.builder().email(request.getEmail()).userId(UUID.randomUUID().toString())
				.name(request.getName()).password(passwordEncoder.encode(request.getPassword()))
				.isAccountVerified(false).resetOtpExpireAt(0L).verifyOtp(null).verifyOtpExpireAt(0L).resetOtp(null)
				.build();

	}

	@Override
	public ProfileResponse getProfile(String email) {
		UserEntity existingUser = userRepository.findByEmail(email).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));
		return convertToProfileResponse(existingUser);

	}

	@Override
	public void sendResetOtp(String email) {

		// 1. Check if user exists
		UserEntity user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found" + email));

		// Generate 6 digit otp
		String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

		// calculate expire time (current time + 5min in millisecs)
		long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

		// update the profiler/user
		// 4. Save OTP and expire time to the user

		user.setResetOtp(otp);
		user.setResetOtpExpireAt(expiryTime);
		// save into database
		userRepository.save(user);

		try {
			// TODO : send the reset otp email
			emailService.sendResetOtpEmail(user.getEmail(), otp);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to send email");
		}
	}

	@Override
	public void resetPassword(String email, String otp, String newPassword) {
		
		// 1. Check if user exists
	    UserEntity existingUser = userRepository.findByEmail(email)
	        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
	    

	    // 2. Validate OTP
	    if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
	        throw new RuntimeException("Invalid OTP");
	    }
	    
	    // 3. Check if OTP is expired
	    if (System.currentTimeMillis() > existingUser.getResetOtpExpireAt()) {
	        throw new RuntimeException("OTP has expired. Please request a new one.");
	    }
	    
	    // 4. Encode the new password
	    existingUser.setPassword( passwordEncoder.encode(newPassword));
	   
	    
	    // 5. Clear the OTP after successful reset
	    existingUser.setResetOtp(null);
	    existingUser.setResetOtpExpireAt(0L);
	    
	    // 6. Save the updated user
	    userRepository.save(existingUser);
	    
	

	}

	@Override
	public void sendOtp(String email) {
		 // 1. Check if user exists
	    UserEntity existingUser = userRepository.findByEmail(email)
	        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

		if(existingUser.getIsAccountVerified()!=null && existingUser.getIsAccountVerified()) {
			return;
		}
		
		//Generate 6 digit OTP
		String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

		// calculate expire time (current time + 24 hours in millisecs)
		long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
		
		//update the UserEntity
		existingUser.setVerifyOtp(otp);
		
		existingUser.setVerifyOtpExpireAt(expiryTime);

		// 6. Save changes in DB
		userRepository.save(existingUser);

		
		
		 // 7. Send email
	    try {
	        emailService.sendOtpEmail(existingUser.getEmail(), otp); 
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to send OTP email", ex);
	    }
		
	}

	@Override
	public void verifyOtp(String email, String otp) {
		 // 1. Check if user exists
	    UserEntity existingUser = userRepository.findByEmail(email)
	        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
	   
	    // 3. Validate OTP
	    if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().toString().equals(otp)) {
	        throw new RuntimeException("Invalid OTP");
	    }
	    
	 // 4. Check if OTP is expired
	    if (existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()) {
	        throw new RuntimeException("OTP has expired");
	    }
	    
	 // 5. Mark the account as verified
	    existingUser.setIsAccountVerified(true);

	    // 6. Clear OTP fields
	    existingUser.setVerifyOtp(null);
	    existingUser.setVerifyOtpExpireAt(0L);

	    // 7. Save to DB
	    userRepository.save(existingUser);
		
	}


}
