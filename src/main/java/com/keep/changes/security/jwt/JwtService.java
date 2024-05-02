package com.keep.changes.security.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${JWT_SECRET_KEY}")
	private String JWT_SECRET_KEY;

//	@Value("${JWT_TOKEN_VALIDITY}")
	private long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;
	private long REFRESH_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

//	Check if given user is valid
	public boolean isValid(String token, UserDetails user) {
		String username = extractUsernameFromToken(token);

		return (username.equals(user.getUsername())) && !isTokenExpired(token);
	}

//	Extract username
	public String extractUsernameFromToken(String token) {
		return extractClaim(token, Claims::getSubject);
	}

//	Check whether given token is valid
	private Boolean isTokenExpired(String token) {
		final Date expiration = extractExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

//	Extract token expiration time
	private Date extractExpirationDateFromToken(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

//	Extract claim
	public <T> T extractClaim(String token, Function<Claims, T> resolver) {
		Claims claims = extraxtAllClaims(token);

		return resolver.apply(claims);
	}

//	Extract all claims
	private Claims extraxtAllClaims(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

//	generate Access token method
	public String generateAccessToken(UserDetails user) {
		return generateAccessToken(new HashMap<>(), user);
	}

	public String generateAccessToken(Map<String, Object> extraclaims, UserDetails user) {
		return buildToken(extraclaims, user, JWT_TOKEN_VALIDITY);
	}

//	generate refresh token
	public String generateRefreshToken(UserDetails user) {
		return buildToken(new HashMap<>(), user, REFRESH_TOKEN_VALIDITY);
	}

	public String buildToken(Map<String, Object> extraClaims, UserDetails user, long expiration) {
		return Jwts.builder().subject(user.getUsername()).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + expiration)).signWith(getSigningKey()).compact();
	}

//	create secret key
	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64URL.decode(JWT_SECRET_KEY);

		return Keys.hmacShaKeyFor(keyBytes);
	}

}
