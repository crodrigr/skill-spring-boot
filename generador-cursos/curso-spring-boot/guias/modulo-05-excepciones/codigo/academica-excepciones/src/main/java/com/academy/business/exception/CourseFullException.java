package com.academy.business.exception;

public class CourseFullException extends RuntimeException {

    public CourseFullException(Long courseId, int maxCapacity) {
        super("El curso " + courseId + " ya alcanzó su cupo de " + maxCapacity);
    }
}
