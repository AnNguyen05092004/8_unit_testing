package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    StudentAnswer findByAnswer_IdAndStudentQuiz_Id(Long answerId, Long studentQuizId);
}
