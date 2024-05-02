package com.keep.changes.security.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.keep.changes.exception.ApiException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
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

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

//		1. Get Token
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String token;
		String userName = null;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {

			filterChain.doFilter(request, response);

			return;

		}

//		Get token from header
		token = authHeader.substring(7);

//		Get email / username from token
		try {
			userName = this.jwtService.extractUsernameFromToken(token);
			System.out.println("username");
			if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				System.out.println("here");
				UserDetails userDetails = this.detailsServiceImpl.loadUserByUsername(userName);

				System.out.println("also here");
				if (this.jwtService.isValid(token, userDetails)) {
					System.out.println("is here");

					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					System.out.println("or here");

					usernamePasswordAuthenticationToken
							.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					System.out.println("second");
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
					System.out.println("last");
				}
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Unable to get JWT Token");
			throw new ApiException("Invalid request!! Kindly login again.", HttpStatus.UNAUTHORIZED, false);

		} catch (ExpiredJwtException e) {
			System.out.println("JWT Token was Expired");
			throw new ApiException("Your login session expired!! Kindly login again.", HttpStatus.FORBIDDEN, false);

		} catch (MalformedJwtException e) {
			System.out.println("Invalid JWT Token");
			throw new ApiException("Invalid request. Try logging in again.", HttpStatus.METHOD_NOT_ALLOWED, false);

		}
		System.out.println("finally");
		filterChain.doFilter(request, response);
	}

}
