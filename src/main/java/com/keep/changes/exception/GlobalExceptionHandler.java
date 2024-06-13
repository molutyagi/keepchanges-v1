package com.keep.changes.exception;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.keep.changes.payload.response.ApiResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// handle other exceptions
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException ex) {
		ApiResponse response = new ApiResponse(
				"Authentication failed! Try logging again. " + HttpStatus.UNAUTHORIZED.toString(), false);

		return new ResponseEntity<ApiResponse>(response, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse> apiExceptionHandler(ApiException ex) {
		String msg = ex.getMessage();
		HttpStatus status = ex.getStatus();
		Boolean bool = ex.getBool();
		ApiResponse apiResponse = new ApiResponse(msg, bool);
		return new ResponseEntity<ApiResponse>(apiResponse, status);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleSecurityException(Exception exception) {
		ProblemDetail errorDetail = null;

		String msg = null;

		// TODO send this stack trace to an observability tool
		exception.printStackTrace();

		if (exception instanceof Unauthorized) {
			System.out.println("1 . ");
			msg = exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.UNAUTHORIZED);

		}

		if (exception instanceof BadCredentialsException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The username or password is incorrect");
			System.out.println("2 . ");

			msg = exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.UNAUTHORIZED);
//			return errorDetail;
		}

		if (exception instanceof AccountStatusException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "The account is locked");

			System.out.println("3 . ");
			msg = exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.FORBIDDEN);
		}

		if (exception instanceof AccessDeniedException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "You are not authorized to access this resource");
			System.out.println("4 . ");

			msg = exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.FORBIDDEN);
		}

		if (exception instanceof SignatureException) {
//			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
//			errorDetail.setProperty("description", "The JWT signature is invalid");
			System.out.println("5 . ");

			msg = exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.UNAUTHORIZED);
		}

		if (exception instanceof ExpiredJwtException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The JWT token has expired");
			System.out.println("6 . ");

			msg = exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.UNAUTHORIZED);
		}

		if (exception instanceof InternalAuthenticationServiceException) {
			System.out.println("7 . ");
			msg = exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.UNAUTHORIZED);
		}

		if (exception instanceof MultipartException) {
			System.out.println("8 . ");
			String message = exception.getMessage();
			ApiResponse apiResponse = new ApiResponse(message, false);
			return new ResponseEntity<ApiResponse>(apiResponse, HttpStatus.BAD_REQUEST);
		}

		if (exception instanceof MissingServletRequestPartException) {
			System.out.println("9 yaha se ja rha h");
			String message = exception.getMessage();
			ApiResponse apiResponse = new ApiResponse(message, false);
			return new ResponseEntity<ApiResponse>(apiResponse, HttpStatus.BAD_REQUEST);
		}

		if (exception instanceof NullPointerException) {
			System.out.println("10 . ");
			msg = exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (exception instanceof ResourceNotFoundException) {
			System.out.println("11 . ");
			msg = exception.getMessage();
			ApiResponse apiResponse = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(apiResponse, HttpStatus.NOT_FOUND);
		}

		if (exception instanceof ResourceAlreadyExistsException) {
			System.out.println("12 . ");
			msg = exception.getMessage();
			ApiResponse apiResponse = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(apiResponse, HttpStatus.CONFLICT);
		}

		if (exception instanceof UsernameNotFoundException) {
			System.out.println("13 . ");
			msg = "Unauthorized: Invalid request. Kindly Login / Register first.";
			ApiResponse apiResponse = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(apiResponse, HttpStatus.UNAUTHORIZED);
		}

		if (exception instanceof MethodArgumentNotValidException ex) {
			System.out.println("14 . ");
			Map<String, String> respErrors = new HashMap<>();
			ex.getBindingResult().getAllErrors().forEach((error) -> {
				String field = ((FieldError) error).getField();
				String localMsg = ((FieldError) error).getDefaultMessage();
				respErrors.put(field, localMsg);
			});
			return new ResponseEntity<Map<String, String>>(respErrors, HttpStatus.BAD_REQUEST);
		}

		if (exception instanceof ConstraintViolationException ex) {
			System.out.println("15 . ");
			Map<String, String> errors = ex.getConstraintViolations().stream()
					.collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(),
							ConstraintViolation::getMessage, (existing, replacement) -> existing));
			return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
		}

		if (exception instanceof DataIntegrityViolationException) {
			System.out.println("16 . ");
			HttpStatus status;
			if (exception.getMessage().contains("Duplicate entry")) {
				msg = "Error: A category with the same name already exists.";
				status = HttpStatus.CONFLICT;
			} else {
				msg = "Unexpected Data integrity violation occurred.";
				status = HttpStatus.BAD_REQUEST;
			}
			ApiResponse apiResponse = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(apiResponse, status);
		}

		if (exception instanceof MissingServletRequestParameterException) {
			System.out.println("17 . ");
			msg = exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (exception instanceof IOException) {
			System.out.println("18 . ");
			msg = "No such directory found: " + exception.getMessage();
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (exception instanceof MethodArgumentTypeMismatchException ex) {
			System.out.println("19 ");
			msg = String.format("Failed to convert value of parameter '%s' to required type '%s'.", ex.getName(),
					ex.getRequiredType().getSimpleName());
			ApiResponse response = new ApiResponse(msg, false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (exception instanceof NoResourceFoundException) {
			System.out.println("20");
			ApiResponse response = new ApiResponse(exception.getMessage(), false);
			return new ResponseEntity<ApiResponse>(response, HttpStatus.NOT_FOUND);
		}

		System.out.println(" 0 ");
		ApiResponse response = new ApiResponse(exception.getMessage(), false);
		return new ResponseEntity<ApiResponse>(response, HttpStatus.UNAUTHORIZED);
	}

}
