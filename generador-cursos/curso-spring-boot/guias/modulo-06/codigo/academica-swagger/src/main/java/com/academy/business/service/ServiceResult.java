package com.academy.business.service;

public record ServiceResult<T>(ServiceStatus status, T value) {

    public static <T> ServiceResult<T> ok(T value) {
        return new ServiceResult<>(ServiceStatus.OK, value);
    }

    public static <T> ServiceResult<T> notFound() {
        return new ServiceResult<>(ServiceStatus.NOT_FOUND, null);
    }

    public static <T> ServiceResult<T> conflict() {
        return new ServiceResult<>(ServiceStatus.CONFLICT, null);
    }
}
