package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.ExplanationQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExplanationQuestionRepository extends JpaRepository<ExplanationQuestion, Long> {
    ExplanationQuestion findByQuestionId(Long questionId);

}
