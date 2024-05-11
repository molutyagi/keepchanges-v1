package com.keep.changes.user.token;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keep.changes.config.AppConstants;
import com.keep.changes.exception.ResourceNotFoundException;

@Service
public class TokenServiceImpl implements TokenService {

	private final int OTP_EXPIRATION_MINUTES = AppConstants.OTP_EXPIRATION_MINUTES;

	@Autowired
	private TokenRepository tokenRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public String generateAndSaveActivationToken(String email) {

		TokenDto tokenDto = this.generateToken(email);

		Token token = this.modelMapper.map(tokenDto, Token.class);
		this.tokenRepository.save(token);

		return tokenDto.getToken();
	}

	@Override
	public Boolean verifyToken(String otp) {

		Token token = this.tokenRepository.findLatestTokenByToken(otp)
				.orElseThrow(() -> new ResourceNotFoundException("email", "otp", otp));

		if (token.getCreatedAt().isBefore(LocalDateTime.now()) && token.getVerified() != true) {
			token.setVerifiedAt(LocalDateTime.now());
			token.setVerified(true);
			this.tokenRepository.save(token);
			return true;
		}
		return false;
	}

	@Override
	public void deleteToken(Long tId) {
		// TODO Auto-generated method stub

	}

//	convert string otp to token
	private TokenDto generateToken(String email) {
		String generatedOtp = this.generateOtp(6);
		TokenDto token = new TokenDto();
		token.setToken(generatedOtp);
		token.setCreatedAt(LocalDateTime.now());
		token.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
		token.setEmail(email);

		return token;
	}

//	generate otp code
	private String generateOtp(int length) {
		String characters = "0123456789";

		StringBuilder codeBuilder = new StringBuilder();
		SecureRandom secureRandom = new SecureRandom();

		for (int i = 0; i < length; i++) {
			int randomIndex = secureRandom.nextInt(characters.length());
			codeBuilder.append(characters.charAt(randomIndex));
		}

		return codeBuilder.toString();
	}

}
