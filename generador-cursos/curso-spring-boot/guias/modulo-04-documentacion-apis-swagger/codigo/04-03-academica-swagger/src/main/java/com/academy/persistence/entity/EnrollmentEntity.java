package com.academy.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollment")
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    protected EnrollmentEntity() {
    }

    public EnrollmentEntity(StudentEntity student, CourseEntity course) {
        this.student = student;
        this.course = course;
    }

    public Long getId() { return id; }
    public StudentEntity getStudent() { return student; }
    public CourseEntity getCourse() { return course; }
}
