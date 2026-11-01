package com.academy.presentation.mapper;

import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.entity.StudentStatus;
import com.academy.presentation.dto.StudentRequest;
import com.academy.presentation.dto.StudentResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StudentMapperTest {

    @Test
    void requestBecomesAnEntityWithoutId() {
        StudentRequest request = new StudentRequest("Ana Torres", 22, "ana@mail.com", StudentStatus.ACTIVE);

        StudentEntity entity = StudentMapper.toEntity(request);

        assertNull(entity.getId());
        assertEquals("Ana Torres", entity.getName());
        assertEquals("ana@mail.com", entity.getEmail());
    }

    @Test
    void entityBecomesAResponseWithTheSameData() {
        StudentEntity entity = new StudentEntity(7L, "Luis Pérez", 25, "luis@mail.com", StudentStatus.INACTIVE);

        StudentResponse response = StudentMapper.toResponse(entity);

        assertEquals(new StudentResponse(7L, "Luis Pérez", 25, "luis@mail.com", StudentStatus.INACTIVE), response);
    }
}
