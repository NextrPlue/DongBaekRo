package com.redstonetorch.dongbaekro.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.redstonetorch.dongbaekro.auth.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final SecretKey secretKey;
	private final long expirationMs;
	private final long refreshExpirationMs;

	public JwtTokenProvider(@Value("${JWT_SECRET}") String secretKey, @Value("${JWT_EXPIRATION}") long expirationMs,
		@Value("${JWT_REFRESH_EXPIRATION}") long refreshExpirationMs) {
		this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
		this.refreshExpirationMs = refreshExpirationMs;
	}

	public String createToken(User user) {
		Claims claims = Jwts.claims().setSubject(user.getId().toString());
		claims.put("name", user.getName());
		Date now = new Date();
		Date validity = new Date(now.getTime() + expirationMs);

		return Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(validity)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}

	public String createRefreshToken(User user) {
		Claims claims = Jwts.claims().setSubject(user.getId().toString());
		Date now = new Date();
		Date validity = new Date(now.getTime() + refreshExpirationMs);

		return Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(validity)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Claims getClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}
}
