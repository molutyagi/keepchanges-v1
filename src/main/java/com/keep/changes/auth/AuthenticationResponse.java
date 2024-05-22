package com.keep.changes.auth;

import lombok.Data;

@Data
public class AuthenticationResponse {

	private String accessToken;
	private String refreshToken;
	private Long userId;
	private String email;
	private String name;
}
