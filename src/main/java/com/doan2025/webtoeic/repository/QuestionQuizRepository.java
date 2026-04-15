package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.QuestionQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionQuizRepository extends JpaRepository<QuestionQuiz, Long> {
    @Query("""
                    SELECT count(*) from QuestionQuiz q
                    WHERE q.quiz.id = :quizId
            """)
    long countByQuizId(Long quizId);

    void deleteQuestionQuizByQuizIdAndQuestionId(Long idQuiz, Long idQuestion);
}
