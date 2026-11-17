package com.academy.business.exception;

public class AlreadyEnrolledException extends RuntimeException {

    public AlreadyEnrolledException(Long studentId, Long courseId) {
        super("El estudiante " + studentId + " ya está matriculado en el curso " + courseId);
    }
}
