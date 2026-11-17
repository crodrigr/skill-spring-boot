package com.academy.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Valida usuario y contraseña (lanza AuthenticationException si no coinciden) y emite el JWT. */
    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return jwtService.generate(authentication.getName(), role);
    }

    public long expirationSeconds() {
        return jwtService.expirationSeconds();
    }
}
