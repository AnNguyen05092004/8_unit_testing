package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.SharedQuiz;
import com.doan2025.webtoeic.dto.SearchQuizDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareQuizRepository extends JpaRepository<SharedQuiz, Long> {

    @Query("""
                    select sq from SharedQuiz sq
                    WHERE (COALESCE(:#{#dto.searchString}, NULL ) is null 
                        OR LOWER(cast(sq.quiz.description as string) ) LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%'))  
                        OR LOWER(cast(sq.quiz.title as string) ) LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%')) 
                        ) AND sq.clazz.id = :idClass
            """)
    Page<SharedQuiz> filter(SearchQuizDto dto, Long idClass, Pageable pageable);

    @Query("""
                    select sq from SharedQuiz sq
                    WHERE (COALESCE(:#{#dto.searchString}, NULL ) is null 
                        OR LOWER(cast(sq.quiz.description as string) ) LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%'))  
                        OR LOWER(cast(sq.quiz.title as string) ) LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%')) 
                        ) AND sq.clazz.id = :idClass
            """)
    List<SharedQuiz> filter(SearchQuizDto dto, Long idClass);


    @Query(value = "WITH  quiz_cte AS (" +
            "SELECT sq.quiz FROM student_quiz sq " +
            "JOIN quiz q ON q.id = sq.quiz " +
            "WHERE (COALESCE(:#{#dto.searchString}, NULL ) is null \n" +
            "   OR LOWER(q.description) LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%'))  \n" +
            "   OR LOWER(q.title) LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%')) \n" +
            " ) AND sq.score >= :score" +
            ")" +
            "SELECT COUNT(DISTINCT sq.quiz_id) FROM shared_quiz sq " +
            "JOIN quiz_cte qc ON qc.quiz = sq.quiz_id " +
            "WHERE sq.class_id = :idClass ",
            nativeQuery = true)
    Long statisticOverviewOverScoreQuizInClass(Long idClass, SearchQuizDto dto, Long score);
}
