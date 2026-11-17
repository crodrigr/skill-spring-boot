package com.academy.persistence.repository;

import com.academy.persistence.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, Long> {

    Optional<UserAccountEntity> findByUsername(String username);
}
