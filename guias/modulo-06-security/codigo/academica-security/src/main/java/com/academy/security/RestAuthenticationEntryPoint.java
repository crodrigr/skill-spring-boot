package com.academy.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Se ejecuta cuando alguien SIN autenticar pide un recurso protegido: responde 401. */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorWriter errors;

    public RestAuthenticationEntryPoint(SecurityErrorWriter errors) {
        this.errors = errors;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object reason = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
        String message = reason != null ? reason.toString() : "Se requiere autenticación para acceder a este recurso";
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        errors.write(request, response, HttpStatus.UNAUTHORIZED, message);
    }
}
