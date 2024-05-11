package com.keep.changes.user.token;

public interface TokenService {

	String generateAndSaveActivationToken(String email);

	Boolean verifyToken(String otp);

	void deleteToken(Long tId);

}
