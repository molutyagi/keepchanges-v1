package com.keep.changes.security.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private final JwtService jwtService;

	@Autowired
	private final UserDetailsServiceImpl detailsServiceImpl;

	@Autowired
	private final HandlerExceptionResolver handlerExceptionResolver;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		System.out.println("Request Method: " + request.getMethod());
		System.out.println("Request URI: " + request.getRequestURI());
		System.out.println("Request Headers: " + Collections.list(request.getHeaderNames()));

		System.out.println("Request Parameters:");
		request.getParameterMap().forEach((key, value) -> {
			System.out.println("Parameter Name: " + key);
			System.out.println("Parameter Value: " + Arrays.toString(value));
		});
//		1. Get Token
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String token;
		String userName = null;

		System.out.println("Token : " + authHeader);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
//		Get token from header
		token = authHeader.substring(7);
//		Get email / username from token
		try {
			userName = this.jwtService.extractUsernameFromToken(token);
			if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = this.detailsServiceImpl.loadUserByUsername(userName);
				if (this.jwtService.isValid(token, userDetails)) {
					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					usernamePasswordAuthenticationToken
							.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				}
			}

			filterChain.doFilter(request, response);
		} catch (Exception exception) {
			exception.printStackTrace();
			handlerExceptionResolver.resolveException(request, response, null, exception);
		}
	}

}
