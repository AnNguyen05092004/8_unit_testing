package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.CheckInit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInitRepository extends JpaRepository<CheckInit, Long> {
    boolean findByCode(String code);

    boolean existsByCode(String code);
}
