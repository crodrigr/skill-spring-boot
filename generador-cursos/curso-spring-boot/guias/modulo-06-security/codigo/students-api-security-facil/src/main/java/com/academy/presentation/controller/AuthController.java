package com.academy.presentation.controller;

import com.academy.presentation.dto.LoginRequest;
import com.academy.presentation.dto.LoginResponse;
import com.academy.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Iniciar sesión")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(summary = "Iniciar sesión",
            description = "Devuelve un token. Usuarios de práctica: `admin` / `admin123` (ADMIN) y `ana` / `ana123` (USER).")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String token = service.login(request.username(), request.password());
        return new LoginResponse(token, "Bearer", service.expirationSeconds());
    }
}
