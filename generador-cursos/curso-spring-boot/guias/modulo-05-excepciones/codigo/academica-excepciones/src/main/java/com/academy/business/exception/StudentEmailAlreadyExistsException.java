package com.academy.business.exception;

public class StudentEmailAlreadyExistsException extends RuntimeException {

    public StudentEmailAlreadyExistsException(String email) {
        super("El correo " + email + " ya está registrado");
    }
}
