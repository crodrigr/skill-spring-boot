package com.academy.business.exception;

public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(Long id) {
        super("No existe un curso con id " + id);
    }
}
