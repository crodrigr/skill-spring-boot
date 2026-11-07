package com.academy.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Se ejecuta cuando alguien YA autenticado pide algo que su rol no permite: responde 403. */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorWriter errors;

    public RestAccessDeniedHandler(SecurityErrorWriter errors) {
        this.errors = errors;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        errors.write(request, response, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
    }
}
