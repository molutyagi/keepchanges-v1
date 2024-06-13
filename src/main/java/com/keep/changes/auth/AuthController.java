package com.keep.changes.auth;

import java.io.IOException;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keep.changes.exception.ApiException;
import com.keep.changes.user.UserDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("auth")
public class AuthController {

	@Autowired
	private AuthenticationService authenticationService;

	@PostMapping("login")
	public ResponseEntity<AuthenticationResponse> login(
			@Valid @RequestBody AuthenticationRequest authenticationRequest) {

		AuthenticationResponse responseTokens = null;
		try {
			System.out.println("in login");
			responseTokens = this.authenticationService.login(authenticationRequest);
		} catch (AuthenticationException e) {
			throw new ApiException("An error occured logging you. Kindly recheck your credentials and try again.",
					HttpStatus.BAD_REQUEST, false);
		}

		System.out.println(responseTokens);
		return ResponseEntity.ok(responseTokens);
	}

	@PostMapping("register")
	public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody UserDto userDto) {
		return ResponseEntity.ok(this.authenticationService.register(userDto));
	}

	@PostMapping("refresh-token")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.authenticationService.refreshToken(request, response);
	}

	@PostMapping("reset-password")
	public ResponseEntity<AuthenticationResponse> resetPassword(
			@Valid @RequestBody AuthenticationRequest authenticationRequest) {
		return ResponseEntity.ok(this.authenticationService.resetPassword(authenticationRequest));
	}

}