package com.keep.changes.auth;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Optional;

import javax.naming.AuthenticationException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keep.changes.config.AppConstants;
import com.keep.changes.exception.ApiException;
import com.keep.changes.exception.ResourceAlreadyExistsException;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.role.Role;
import com.keep.changes.role.RoleRepository;
import com.keep.changes.security.jwt.JwtService;
import com.keep.changes.user.User;
import com.keep.changes.user.UserDto;
import com.keep.changes.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Override
	public AuthenticationResponse register(UserDto userDto) {

		Optional<User> userWithEmail = this.userRepository.findByEmail(userDto.getEmail());
		if (userWithEmail.isPresent()) {
			throw new ResourceAlreadyExistsException("User", "Email", userDto.getEmail());
		}

		Optional<User> userWithPhone = this.userRepository.findByPhone(userDto.getPhone());
		if (userWithPhone.isPresent()) {
			throw new ResourceAlreadyExistsException("User", "Phone", userDto.getPhone());
		}

		User newUser = this.modelMapper.map(userDto, User.class);
		newUser.setPassword(this.passwordEncoder.encode(userDto.getPassword()));

		Role role = this.roleRepository.findById(AppConstants.NORMAL_USER)
				.orElseThrow(() -> new ResourceNotFoundException("Role", "Id", AppConstants.NORMAL_USER));

		System.out.println("role : " + role);
		newUser.getRoles().add(role);

//		sendValidationEmail(newUser);

		User savedUser = this.userRepository.save(newUser);

		System.out.println(savedUser);

		String accessToken = this.jwtService.generateAccessToken(savedUser);
		String refreshToken = this.jwtService.generateRefreshToken(savedUser);

		AuthenticationResponse response = new AuthenticationResponse();
		response.setAccessToken(accessToken);
		response.setRefreshToken(refreshToken);

		return response;

	}

	@Override
	public AuthenticationResponse login(AuthenticationRequest userRequest) throws AuthenticationException {

		try {
			this.authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));
		} catch (BadCredentialsException e) {
			throw new ApiException("Invalid Password. Enter correct password.", HttpStatus.BAD_REQUEST, false);
		}

		UserDetails userDetails = this.userDetailsService.loadUserByUsername(userRequest.getUsername());

		String accessToken = this.jwtService.generateAccessToken(userDetails);
		String refreshToken = this.jwtService.generateRefreshToken(userDetails);

		AuthenticationResponse response = new AuthenticationResponse();
		response.setAccessToken(accessToken);
		response.setRefreshToken(refreshToken);

		return response;
	}

	@Override
	public void refreshToken(HttpServletRequest request, HttpServletResponse response)
			throws StreamWriteException, DatabindException, IOException {

		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String refreshToken;
		final String userName;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}

		refreshToken = authHeader.substring(7);
		userName = this.jwtService.extractUsernameFromToken(refreshToken);
		if (userName != null) {

			UserDetails userDetails = this.userRepository.findByEmail(userName)
					.orElseThrow(() -> new ResourceNotFoundException("User", "Username", userName));

			if (this.jwtService.isValid(refreshToken, userDetails)) {

				String accessToken = this.jwtService.generateAccessToken(userDetails);
				AuthenticationResponse authResponse = new AuthenticationResponse();
				authResponse.setAccessToken(accessToken);
				authResponse.setRefreshToken(refreshToken);

				new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
			}
		}

	}

//	verify the email using otp
	private void sendValidationEmail(User newUser) {

		var token = generateAndSaveActivationToken(newUser);
//		send email

	}

	private String generateAndSaveActivationToken(User newUser) {

		String generatedCode = generateActivationCode(6);

		return "";
	}

	private String generateActivationCode(int length) {

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
