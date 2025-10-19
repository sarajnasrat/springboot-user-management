package com.mcit.usermanagement.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTGenerator tokenGenerator; // Utility class for token operations

    @Autowired
    private CustomUserDetailsService customUserDetailsService; // To fetch user details

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Exclude refresh token requests from JWT validation
        if (requestURI.equals("/api/auth/refresh-token")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract the JWT token from the request
            String token = getJWTFromRequest(request);

            if (StringUtils.hasText(token)) {
                if (tokenGenerator.validateAccessToken(token)) {
                    // Extract the username from the token
                    String username = tokenGenerator.getUsernameFromAccessToken(token);

                    // Load user details using CustomUserDetailsService
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                    // Set authentication in the security context
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the authentication to SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    throw new AuthenticationCredentialsNotFoundException("Invalid or expired JWT");
                }
            }

            // Proceed with the filter chain
            filterChain.doFilter(request, response);
        } catch (AuthenticationCredentialsNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getOutputStream().write(("{\"error\": \"Unauthorized: " + e.getMessage() + "\"}").getBytes());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getOutputStream()
                    .write(("{\"error\": \"Internal server error: " + e.getMessage() + "\"}").getBytes());
        }
    }

    // Helper method to extract the JWT token from the Authorization header
    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
