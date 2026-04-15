package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultantRepository extends JpaRepository<Consultant, Long> {
}
