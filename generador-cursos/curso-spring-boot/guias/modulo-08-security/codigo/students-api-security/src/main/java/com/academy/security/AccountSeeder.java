package com.academy.security;

import com.academy.persistence.entity.Role;
import com.academy.persistence.entity.UserAccountEntity;
import com.academy.persistence.repository.UserAccountJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea dos cuentas de práctica al arrancar. SOLO PARA FINES EDUCATIVOS:
 * en un proyecto real las cuentas no se siembran con contraseñas en el código.
 */
@Component
public class AccountSeeder implements CommandLineRunner {

    private final UserAccountJpaRepository accounts;
    private final PasswordEncoder encoder;

    public AccountSeeder(UserAccountJpaRepository accounts, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing("admin", "admin123", Role.ADMIN);
        createIfMissing("ana", "ana123", Role.USER);
    }

    private void createIfMissing(String username, String rawPassword, Role role) {
        if (accounts.findByUsername(username).isEmpty()) {
            accounts.save(new UserAccountEntity(null, username, encoder.encode(rawPassword), role));
        }
    }
}
