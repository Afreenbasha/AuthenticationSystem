package com.springboot.fullstackproject.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.springboot.fullstackproject.io.AuthRequest;
import com.springboot.fullstackproject.io.AuthResponse;
import com.springboot.fullstackproject.io.ResetPasswordRequest;
import com.springboot.fullstackproject.service.AppUserDetailsService;
import com.springboot.fullstackproject.service.ProfileService;
import com.springboot.fullstackproject.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "true")

public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final AppUserDetailsService appUserDetailsService;
	private final JwtUtil jwtUtil;

	private final ProfileService profileService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthRequest request) {
		try {
			authenticate(request.getEmail(), request.getPassword());
			final UserDetails userDetails = appUserDetailsService.loadUserByUsername(request.getEmail());
			final String jwtToken = jwtUtil.generateToken(userDetails);
			ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken).httpOnly(true).path("/")
					.maxAge(Duration.ofDays(1)).sameSite("Strict").build();
			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
					.body(new AuthResponse(request.getEmail(), jwtToken));

		} catch (BadCredentialsException ex) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", true);
			error.put("Message", "Email or password is incorrect");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
		} catch (DisabledException ex) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", true);
			error.put("Message", "Account is Disabled");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		} catch (Exception ex) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", true);
			error.put("Message", "Authentication failed");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		}
	}

	private void authenticate(String email, String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

	}

	@GetMapping("/is-authenticated")
	public ResponseEntity<Boolean> isAuthenticated(
			@CurrentSecurityContext(expression = "authentication?.name") String email) {
		// If email is present, user is authenticated
		// boolean isAuthenticated = (email != null && !email.isEmpty());
		return ResponseEntity.ok(email != null);
	}

	@PostMapping("/send-reset-otp")
	public void sendResetOtp(@RequestParam String email) {
		try {
			profileService.sendResetOtp(email);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@PostMapping("/reset-password")
	public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		try {
			profileService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
	
	@PostMapping("/send-otp")
	public void sendVerifyOtp(@CurrentSecurityContext(expression="authentication?.name")String email) {
		 try {
		        profileService.sendOtp(email);
		    } catch (Exception e) {
		        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		    }
	}
	
	@PostMapping("/verify-otp")
	public void verifyEmail(@RequestBody Map<String ,Object>request ,@CurrentSecurityContext(expression="authentication?.name")String email) {
		  if (request.get("otp") == null || request.get("otp").toString().isBlank()) {
		        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is missing");
		    }
		
		try {
			profileService.verifyOtp(email, request.get("otp").toString());
		}
		catch(Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
		}
	}
	
	@PostMapping("/logout")
	public ResponseEntity<?>logout(HttpServletResponse response){
		ResponseCookie cookie=ResponseCookie.from("jwt","")
				.httpOnly(true)
				.secure(false)
				.path("/")
				.maxAge(0)
				.sameSite("Lax")
				.build();
		
		  return ResponseEntity.ok()
		            .header(HttpHeaders.SET_COOKIE, cookie.toString())
		            .body( "Logged out successfully");
		
	}

}
