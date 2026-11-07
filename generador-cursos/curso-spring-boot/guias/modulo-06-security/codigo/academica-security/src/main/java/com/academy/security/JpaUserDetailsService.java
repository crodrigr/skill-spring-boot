package com.academy.security;

import com.academy.persistence.entity.UserAccountEntity;
import com.academy.persistence.repository.UserAccountJpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserAccountJpaRepository accounts;

    public JpaUserDetailsService(UserAccountJpaRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccountEntity account = accounts.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No existe la cuenta " + username));
        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .roles(account.getRole().name())
                .build();
    }
}
