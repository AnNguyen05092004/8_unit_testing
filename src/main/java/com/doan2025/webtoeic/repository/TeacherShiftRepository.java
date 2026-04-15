package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.TeacherShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherShiftRepository extends JpaRepository<TeacherShift, Long> {
}
