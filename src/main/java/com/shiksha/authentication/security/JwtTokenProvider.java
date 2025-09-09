package com.shiksha.authentication.security;

import com.shiksha.authentication.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final int jwtExpirationInHours;
    private final int refreshTokenExpirationInDays;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:mySecretKey}") String jwtSecret,
            @Value("${app.jwt.expiration-hours:24}") int jwtExpirationInHours,
            @Value("${app.jwt.refresh-expiration-days:7}") int refreshTokenExpirationInDays) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationInHours = jwtExpirationInHours;
        this.refreshTokenExpirationInDays = refreshTokenExpirationInDays;
    }

    public String generateAccessToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        return generateAccessToken(userPrincipal);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtExpirationInHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpirationInDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("tokenType", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    public String getUserEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty");
        }
        return false;
    }

    public int getJwtExpirationInHours() {
        return jwtExpirationInHours;
    }
}