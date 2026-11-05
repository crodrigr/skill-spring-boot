package com.academy.business.exception;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(String message) {
        super(message);
    }

    public static StudentNotFoundException byId(Long id) {
        return new StudentNotFoundException("No existe un estudiante con id " + id);
    }

    public static StudentNotFoundException byEmail(String email) {
        return new StudentNotFoundException("No existe un estudiante con el correo " + email);
    }
}
