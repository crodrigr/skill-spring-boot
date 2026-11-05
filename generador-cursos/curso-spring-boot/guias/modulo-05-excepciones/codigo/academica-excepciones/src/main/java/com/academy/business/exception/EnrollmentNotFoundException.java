package com.academy.business.exception;

public class EnrollmentNotFoundException extends RuntimeException {

    public EnrollmentNotFoundException(Long id) {
        super("No existe una matrícula con id " + id);
    }
}
