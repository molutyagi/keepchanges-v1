package com.keep.changes.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.keep.changes.exception.ApiException;
import com.keep.changes.exception.ResourceAlreadyExistsException;
import com.keep.changes.payload.response.ApiResponse;
import com.keep.changes.user.UserDto;
import com.keep.changes.user.UserService;
import com.keep.changes.user.token.TokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("auth/verification")
public class EmailVerificationController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private UserService userService;

	@PostMapping("send-otp")
	public ResponseEntity<?> sendOtp(@Valid @RequestBody GetOtpDto getOtpDto) {

		if (getOtpDto.getAction().equals("register-user")) {
			if (this.userService.getUserByEmail(getOtpDto.getEmail()) != null) {
				throw new ResourceAlreadyExistsException("User", "Email", getOtpDto.getEmail());
			}

			if (this.userService.getUserByPhone(getOtpDto.getPhone()) != null) {
				throw new ResourceAlreadyExistsException("User", "phone", getOtpDto.getPhone());
			}

			if (this.userService.emailExists(getOtpDto.getEmail())) {
				throw new ResourceAlreadyExistsException("User", "Email", getOtpDto.getEmail());
			}
			if (this.userService.phoneExists(getOtpDto.getPhone())) {
				throw new ResourceAlreadyExistsException("User", "Phone", getOtpDto.getPhone());
			}
			this.emailService.sendEmail(getOtpDto.getEmail(), getOtpDto.getName(), EmailTemplateName.CONFIRM_EMAIL,
					"email-verification");
		} else if (getOtpDto.getAction().equals("reset-password")) {
			UserDto user = this.userService.getUserByEmail(getOtpDto.getEmail());
			this.emailService.sendEmail(getOtpDto.getEmail(), user.getName(), EmailTemplateName.RESET_PASSWORD,
					"reset-password");
		}
		return ResponseEntity.ok(new ApiResponse("OTP sent to your mail successfully", true));
	}

	@PostMapping("verify-otp")
	public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpDto verifyOtpDto) {
		System.out.println(verifyOtpDto.getOtp());
		if (this.tokenService.verifyToken(verifyOtpDto.getOtp())) {
			return ResponseEntity.ok(new ApiResponse("Email verified successfully", true));
		}
		throw new ApiException("OTP expired. Get a new OTP and retry again.", HttpStatus.BAD_REQUEST, false);
	}
}
