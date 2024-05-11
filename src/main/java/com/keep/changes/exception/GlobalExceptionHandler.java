package com.keep.changes.exception;

import java.util.HashMap;
import java.util.Map;

import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import com.keep.changes.payload.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler({ AuthenticationException.class })
	public ResponseEntity<ApiResponse> handleAuthenticationException(Exception ex) {
		ApiResponse response = new ApiResponse(
				"Authentication failed! Try logging again. " + HttpStatus.UNAUTHORIZED.toString(), false);

		return new ResponseEntity<ApiResponse>(response, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse> resourceNotFoundExceptionHandler(ResourceNotFoundException ex) {

		String msg = ex.getMessage();
		ApiResponse apiResponse = new ApiResponse(msg, false);

		return new ResponseEntity<ApiResponse>(apiResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ResourceAlreadyExistsException.class)
	public ResponseEntity<ApiResponse> resourceAlreadyExistsExceptionHandler(ResourceAlreadyExistsException ex) {

		String msg = ex.getMessage();
		ApiResponse apiResponse = new ApiResponse(msg, false);

		return new ResponseEntity<ApiResponse>(apiResponse, HttpStatus.ALREADY_REPORTED);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> methodArgumentNotValidExceptionHandler(
			MethodArgumentNotValidException ex) {

		Map<String, String> respErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String field = ((FieldError) error).getField();
			String msg = ((FieldError) error).getDefaultMessage();
			respErrors.put(field, msg);
		});

		return new ResponseEntity<Map<String, String>>(respErrors, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<ApiResponse> multipartFileExceptionHandler(MultipartException ex) {
		String message = ex.getMessage();
		ApiResponse apiResponse = new ApiResponse(message, false);
		return new ResponseEntity<ApiResponse>(apiResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse> apiExceptionHandler(ApiException ex) {

		String msg = ex.getMessage();
		HttpStatus status = ex.getStatus();
		Boolean bool = ex.getBool();

		ApiResponse apiResponse = new ApiResponse(msg, bool);

		return new ResponseEntity<ApiResponse>(apiResponse, status);
	}

}
