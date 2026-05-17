package com.contactapp.backend.config;

import com.contactapp.backend.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// REMOVED @Component annotation to break circular dependency!
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log =
        LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Regular constructor (no Lombok)
    public JwtAuthFilter(JwtUtil jwtUtil,
                         UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || 
            !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractEmail(token);
        log.debug("JWT valid for user: {}", email);

        if (email != null &&
            SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null,
                    userDetails.getAuthorities());

            authToken.setDetails(
                new WebAuthenticationDetailsSource()
                    .buildDetails(request));

            SecurityContextHolder.getContext()
                .setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}