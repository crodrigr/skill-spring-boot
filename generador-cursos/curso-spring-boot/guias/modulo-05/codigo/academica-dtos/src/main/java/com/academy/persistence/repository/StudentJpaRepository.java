package com.academy.persistence.repository;

import com.academy.persistence.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentJpaRepository extends JpaRepository<StudentEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
