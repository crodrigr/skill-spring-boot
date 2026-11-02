package com.academy.presentation.exception;

import com.academy.business.exception.StudentEmailAlreadyExistsException;
import com.academy.business.exception.StudentNotFoundException;
import com.academy.presentation.dto.ErrorResponse;
import com.academy.presentation.dto.ErrorResponse.FieldIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- Excepciones de la aplicación ----------

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(StudentNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(StudentEmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleDuplicateEmail(StudentEmailAlreadyExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    // El login falla dentro de un controlador (AuthenticationManager), así que SÍ pasa por este manejador.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request, null);
    }

    // Sin este manejador, el catch-all convertiría un AccessDeniedException en un 500.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación", request, null);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Object> handleUnknownSortProperty(PropertyReferenceException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "No se puede ordenar por '" + ex.getPropertyName() + "'", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Error inesperado en {}", path(request), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intentar de nuevo más tarde.", request, null);
    }

    // ---------- Excepciones de Spring MVC (las maneja la clase base; aquí se les da el cuerpo propio) ----------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<FieldIssue> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldIssue(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldIssue::field).thenComparing(FieldIssue::message))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", request, details);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido o tiene un valor no permitido", request, null);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getPropertyName() + "' tiene un valor no válido: " + ex.getValue(),
                request, null);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers, HttpStatusCode status,
                                                                          WebRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request, null);
    }

    // Todas las demás excepciones de Spring MVC (ruta inexistente, método no permitido...) pasan por aquí.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        String message = switch (status) {
            case NOT_FOUND -> "No existe el recurso solicitado";
            case METHOD_NOT_ALLOWED -> "Ese método HTTP no está permitido en esta ruta";
            case UNSUPPORTED_MEDIA_TYPE -> "El tipo de contenido enviado no está soportado";
            case NOT_ACCEPTABLE -> "No se puede responder en el formato pedido";
            default -> status.getReasonPhrase();
        };
        ResponseEntity<Object> response = build(status, message, request, null);
        return ResponseEntity.status(status).headers(headers).body(response.getBody());
    }

    // ---------- Apoyo ----------

    private ResponseEntity<Object> build(HttpStatus status, String message, WebRequest request,
                                         List<FieldIssue> details) {
        ErrorResponse body = details == null
                ? ErrorResponse.of(status, message, path(request))
                : ErrorResponse.of(status, message, path(request), details);
        return ResponseEntity.status(status).body(body);
    }

    private String path(WebRequest request) {
        return request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI()
                : "";
    }
}
