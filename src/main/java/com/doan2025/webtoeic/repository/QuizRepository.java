package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Quiz;
import com.doan2025.webtoeic.dto.SearchQuizDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("""
                    SELECT quiz from Quiz quiz
                    WHERE (COALESCE(:#{#dto.searchString}, null) IS NULL OR (
                            LOWER(CAST(quiz.title as string)) like LOWER(CONCAT('%', :#{#dto.searchString}, '%'))
                            OR LOWER(CAST(quiz.description as string)) like LOWER(CONCAT('%', :#{#dto.searchString}, '%'))
                    ) )
            """)
    Page<Quiz> filter(SearchQuizDto dto, Pageable pageable);
}
