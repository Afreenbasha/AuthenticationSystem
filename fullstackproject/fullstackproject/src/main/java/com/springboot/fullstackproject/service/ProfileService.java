package com.springboot.fullstackproject.service;

import com.springboot.fullstackproject.io.ProfileRequest;
import com.springboot.fullstackproject.io.ProfileResponse;

public interface ProfileService {
	ProfileResponse createProfile(ProfileRequest request);
	ProfileResponse getProfile(String email);
	
	//define a method to send reset otp
	
	void sendResetOtp(String email);
	
	//define a method reset password
	void resetPassword(String email,String otp,String newPassword);
	
	void sendOtp(String email);
	
	void verifyOtp(String email,String otp);
	
	

}
