package com.academy.security;

import com.academy.persistence.entity.UserAccountEntity;
import com.academy.persistence.repository.UserAccountJpaRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserAccountJpaRepository accounts;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserAccountJpaRepository accounts, PasswordEncoder encoder, JwtService jwtService) {
        this.accounts = accounts;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    /** Si el usuario y la contraseña son correctos, devuelve un token. Si no, lanza BadCredentialsException. */
    public String login(String username, String password) {
        UserAccountEntity account = accounts.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));
        if (!encoder.matches(password, account.getPasswordHash())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }
        return jwtService.create(account.getUsername(), account.getRole().name());
    }

    public long expirationSeconds() {
        return jwtService.expirationSeconds();
    }
}
