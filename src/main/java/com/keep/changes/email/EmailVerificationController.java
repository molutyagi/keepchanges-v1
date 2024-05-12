package com.keep.changes.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.keep.changes.exception.ResourceAlreadyExistsException;
import com.keep.changes.payload.response.ApiResponse;
import com.keep.changes.user.UserDto;
import com.keep.changes.user.UserService;
import com.keep.changes.user.token.TokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/auth/verification")
public class EmailVerificationController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private UserService userService;

	@PostMapping("send-otp")
	public ResponseEntity<?> sendOtp(@Valid @RequestBody GetOtpDto getOtpDto
//			, @RequestParam("action") String action
	) {

//		if (action.equals("register-user")) {
//		if (this.userService.getUserByEmail(otpDto.getEmail()) != null) {
//			throw new ResourceAlreadyExistsException("User", "Email", otpDto.getEmail());
//		}
//
//		if (this.userService.getUserByPhone(otpDto.getPhone()) != null) {
//			throw new ResourceAlreadyExistsException("User", "phone", otpDto.getPhone());
//		}

		if (this.userService.emailExists(getOtpDto.getEmail())) {
			throw new ResourceAlreadyExistsException("User", "Email", getOtpDto.getEmail());
		}
		if (this.userService.phoneExists(getOtpDto.getPhone())) {
			throw new ResourceAlreadyExistsException("User", "Phone", getOtpDto.getPhone());
		}
		this.emailService.sendEmail(getOtpDto.getEmail(), getOtpDto.getName(), EmailTemplateName.CONFIRM_EMAIL,
				"email-verification");
//		} else if (action.equals("forgot-password")) {
//			UserDto user = this.userService.getUserByEmail(email);
//			this.emailService.sendEmail(email, user.getName(), EmailTemplateName.RESET_PASSWORD, "reset-password");
//
//		}

		return new ResponseEntity(new ApiResponse("OTP sent to your mail successfully", true), HttpStatus.OK);
	}

	@PostMapping("verify-otp")
	public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpDto verifyOtpDto) {
		System.out.println(verifyOtpDto.getOtp());
		if (this.tokenService.verifyToken(verifyOtpDto.getOtp())) {
			return new ResponseEntity(new ApiResponse("Email verified successfully", true), HttpStatus.OK);
		}
		return new ResponseEntity(new ApiResponse("OTP expired. Get a new OTP and retry again.", false),
				HttpStatus.BAD_REQUEST);
	}

	@PostMapping("reset-password")
	public ResponseEntity<?> resetPassword(@Valid @RequestParam("email") String email,
			@RequestParam("password") String password) {

		UserDto user = this.userService.getUserByEmail(email);
		user.setPassword(password);
		this.userService.patchUpdateUser(user.getId(), user);

		return new ResponseEntity(new ApiResponse("Password updated successfully", true), HttpStatus.OK);
	}
}
