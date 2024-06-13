package com.keep.changes.auth;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationRequest {

	private String username;

	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?!.*\\s).{8,32}$", message = "Password must be between 8 - 32 characters long and must contain at least one letter, one number, and one special character")
	private String password;

}
