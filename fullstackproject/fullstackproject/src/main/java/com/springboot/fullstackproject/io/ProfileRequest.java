package com.springboot.fullstackproject.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileRequest {

	@NotBlank(message="Name should not be empty ")
	private String name;
	
	@Email(message="Enter valid email address")
	@NotNull(message="Email should not be empty")
	private String email;
	@Size(min=6 , message="Password must be  atleast 6 characters")
	private String password;

}