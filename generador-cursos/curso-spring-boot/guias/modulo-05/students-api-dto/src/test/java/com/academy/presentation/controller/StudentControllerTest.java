package com.academy.presentation.controller;

import com.academy.business.service.ServiceResult;
import com.academy.business.service.StudentService;
import com.academy.persistence.entity.StudentEntity;
import com.academy.persistence.entity.StudentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    private static final String VALID_BODY =
            "{\"name\":\"Ana Torres\",\"age\":22,\"email\":\"ana@mail.com\",\"status\":\"ACTIVE\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StudentService service;

    @Test
    void createReturns201WithLocationAndNoInternalFields() throws Exception {
        StudentEntity saved = new StudentEntity(5L, "Ana Torres", 22, "ana@mail.com", StudentStatus.ACTIVE);
        when(service.create(any())).thenReturn(ServiceResult.ok(saved));

        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/students/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.createdAt").doesNotExist());
    }

    @Test
    void createReturns409WhenTheEmailExists() throws Exception {
        when(service.create(any())).thenReturn(ServiceResult.conflict());

        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void createReturns400WhenTheBodyIsInvalid() throws Exception {
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"age\":5,\"email\":\"x\",\"status\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByIdReturns404WhenItDoesNotExist() throws Exception {
        when(service.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/students/99")).andExpect(status().isNotFound());
    }

    @Test
    void patchChangesOnlyTheStatus() throws Exception {
        StudentEntity updated = new StudentEntity(1L, "Ana Torres", 22, "ana@mail.com", StudentStatus.INACTIVE);
        when(service.changeStatus(eq(1L), eq(StudentStatus.INACTIVE))).thenReturn(ServiceResult.ok(updated));

        mvc.perform(patch("/api/v1/students/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}
