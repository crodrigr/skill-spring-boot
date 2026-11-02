package com.academy.presentation.controller;

import com.academy.presentation.dto.LoginRequest;
import com.academy.presentation.dto.LoginResponse;
import com.academy.presentation.dto.MeResponse;
import com.academy.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Inicio de sesión y datos del usuario autenticado")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(summary = "Iniciar sesión",
            description = "Valida usuario y contraseña y devuelve un JWT. Cuentas de práctica: `admin` / `admin123` (ADMIN) y `ana` / `ana123` (USER).")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Sesión iniciada: devuelve el token")
    @ApiResponse(responseCode = "400", ref = "BadRequest")
    @ApiResponse(responseCode = "401", ref = "Unauthorized")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String token = service.login(request.username(), request.password());
        return new LoginResponse(token, "Bearer", service.expirationSeconds());
    }

    @Operation(summary = "Quién soy", description = "Devuelve el usuario y el rol que salen del token enviado.")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado")
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return new MeResponse(authentication.getName(), role);
    }
}
