package com.example.piggy_saving.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String ROLES_CLAIM = "roles";

    private Key signingKey;

    @Value("${jwt.secret}")      // must be Base64 encoded, e.g. "dGhpcy1pcy1hLXNlY3VyZS1zZWNyZXQtd2l0aC1sZW5ndGgtMzIrYnl0ZXM="
    private String base64Secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    // -------------------- Public extractors --------------------
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roles = claims.get(ROLES_CLAIM, List.class);
        return roles != null ? roles : Collections.emptyList();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // -------------------- Token generation --------------------
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        // Refresh token carries ONLY the subject – no extra claims
        return buildToken(new HashMap<>(), userDetails, jwtRefreshExpiration);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateTokenForUser(String userId, String name, String email, String phone,
                                       boolean emailVerified, List<String> roles, UserDetails userDetails) {
        Map<String, Object> claims = buildClaims(userId, name, email, phone, emailVerified, roles);
        return generateToken(claims, userDetails);
    }

    public TokenPair generateTokenPairForUser(String userId, String name, String email,
                                              String phone, boolean emailVerified,
                                              List<String> roles, UserDetails userDetails) {
        Map<String, Object> claims = buildClaims(userId, name, email, phone, emailVerified, roles);
        String accessToken = generateToken(claims, userDetails);
        // Refresh token uses empty claims (only subject)
        String refreshToken = buildToken(new HashMap<>(), userDetails, jwtRefreshExpiration);
        return new TokenPair(accessToken, refreshToken);
    }

    // -------------------- Validation --------------------
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // -------------------- Internal helpers --------------------
    private Map<String, Object> buildClaims(String userId, String name, String email,
                                            String phone, boolean emailVerified, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("name", name);
        claims.put("email", email);
        claims.put("phone", phone);
        claims.put("emailVerified", emailVerified);
        claims.put(ROLES_CLAIM, roles);
        return claims;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())          // algorithm auto-detected
                .compact();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        if (signingKey == null) {
            byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return signingKey;
    }

    public int getAccessTokenExpirationSeconds() {
        return (int) (jwtExpiration / 1000);
    }

    // Inner class for token pair
    public static class TokenPair {
        public final String accessToken;
        public final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}