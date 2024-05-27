package com.keep.changes.security.jwt;

import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ AuthenticationException.class })
	@ResponseBody
	public ResponseEntity<?> handleAuthenticationException(Exception ex) {
		System.out.println("yaha se ja rha kya?");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("Unauthorized: Authentication is required to access this resource.");
	}
}