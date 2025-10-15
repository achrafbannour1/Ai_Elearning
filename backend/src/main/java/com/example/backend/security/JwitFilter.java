package com.example.backend.security;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwitFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {



        if (request.getServletPath().startsWith("/api/payment/webhook")) {
            filterChain.doFilter(request, response);
            return;
        }


        final String authorizationHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        System.out.println("Request URL: " + request.getRequestURI());

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                email = jwtUtil.extractUsername(jwt);
                System.out.println("Extracted JWT Email: " + email);
            } catch (Exception e) {
                System.err.println("JWT extraction failed: " + e.getMessage());
            }
        } else {
            System.out.println("No Bearer token found in Authorization header");
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                if (jwtUtil.isTokenValid(jwt, user.getEmail())) {
                    System.out.println("User found: " + user.getEmail() + ", ID: " + user.getId());
                    // Use email as principal instead of User object
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, user.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Authentication set for: " + email);
                } else {
                    System.err.println("Invalid JWT token for email: " + email);
                }
            } else {
                System.err.println("User not found for email: " + email);
            }
        } else if (email == null) {
            System.out.println("No email extracted from JWT or authentication already set");
        }

        filterChain.doFilter(request, response);
    }
}