package com.mcit.usermanagement.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

@Component
public class JWTGenerator {
    @Autowired
    private CustomUserDetailsService userDetailsService; // Inject UserDetailsService
    private static final String ACCESS_TOKEN_KEY = "W}AEC-iqQ|47vW\\PAkqB-g\"!3LDG[c]fXAamv@#lbo,L@M6LxbVtvtw7TR(=xStr;+qH.I\"yu@X0K-;A[D):Eo(t?Ko@TzXwqi]c.GPuiFe=qz+#5-/Tivz]pP2QU.vB@3,#l5i[YVT4[6JV!LUtx83BfM?+!v(J7P(fjwr7t'Wh0]";
    private static final String REFRESH_TOKEN_KEY = "!-efqwXHT]\";G4UQ9w.;g1H\"Uk}=.6J6B\\6?/A/u/iDp#!.u\\'g|\\\"TLi|UBT'psyj?ePp8q2![\"jrHbw/0LiEI;?.XOYKSH\"0O+';DootOkY.wg?Gf+KgJEeO)t9aK#Wv7uAq!*Z)B\"bfIM7D\\bq4#RWMZUh,O#1lr2U(Htm7'9x8]";

    // Token expiration times
    private static final long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days

    // Convert the secret keys to SecretKey instances
    private SecretKey accessTokenKey = Keys.hmacShaKeyFor(ACCESS_TOKEN_KEY.getBytes());
    private SecretKey refreshTokenKey = Keys.hmacShaKeyFor(REFRESH_TOKEN_KEY.getBytes());

    public Authentication getAuthenticationFromRefreshToken(String refreshToken) {
        // Validate the refresh token
        if (!validateRefreshToken(refreshToken)) {
            throw new AuthenticationCredentialsNotFoundException("Invalid refresh token");
        }
        // Extract the username from the refresh token
        String username = getUsernameFromRefreshToken(refreshToken);
        // Load user details using the username
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // Return an Authentication object with the user details
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // Generate access token
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        // Extract roles (authorities) from authentication
        String roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(","));
        String token = Jwts.builder()
                .setSubject(username)
                .claim("roles", roles) // 👈 Add this line
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(accessTokenKey, SignatureAlgorithm.HS512) // Use SecretKey for signing
                .compact();

        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Generated JWT token is null or empty.");
        }
        return token;
    }

    // Generate refresh token
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(refreshTokenKey, SignatureAlgorithm.HS512) // Use SecretKey for signing
                .compact();
    }

    // Get username from access token
    public String getUsernameFromAccessToken(String token) {
        return getClaimsFromToken(token, accessTokenKey).getSubject();
    }

    // Get username from refresh token
    public String getUsernameFromRefreshToken(String token) {
        return getClaimsFromToken(token, refreshTokenKey).getSubject();
    }

    // Validate access token
    public boolean validateAccessToken(String token) {
        return validateToken(token, accessTokenKey);
    }

    // Validate refresh token
    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshTokenKey);
    }

    // Get claims from a token using a specific key
    private Claims getClaimsFromToken(String token, SecretKey key) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key) // Use the SecretKey
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException ex) {
            throw new AuthenticationCredentialsNotFoundException("Invalid or expired JWT", ex);
        }
    }

    // Validate a token using a specific key
    private boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key) // Use the SecretKey
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            // Return false if the token is invalid or expired
            return false;
        }
    }
}