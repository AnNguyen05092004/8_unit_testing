package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Question;
import com.doan2025.webtoeic.dto.SearchQuestionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("""
                    SELECT q FROM Question q
                    left join q.rangeTopic r
                    left join q.scoreScale sc
                    where (COALESCE(:#{#dto.idQuestionBank}, null ) IS NULL OR q.questionBank.id = :#{#dto.idQuestionBank})
                    AND (COALESCE(:#{#dto.searchString}, null) IS NULL OR LOWER(CAST(q.content as string)) LIKE CONCAT('%',  :#{#dto.searchString}, '%')  )
                    AND (COALESCE(:#{#dto.rangeTopics}, null) IS NULL OR q.rangeTopic.id IN (:#{#dto.rangeTopics})  )
                    AND (COALESCE(:#{#dto.scoreScales}, null) IS NULL OR q.scoreScale.id IN (:#{#dto.scoreScales})  )
                    AND (COALESCE(:#{#dto.isActive}, null) IS NULL OR q.isActive = :#{#dto.isActive}  )
                    AND (COALESCE(:#{#dto.isDelete}, null) IS NULL OR q.isDelete = :#{#dto.isDelete})
            """)
    Page<Question> filterQuestion(SearchQuestionDto dto, Pageable pageable);

    @Query("""
                    SELECT q FROM Question q
                    JOIN QuestionBank qb ON q.questionBank.id = qb.id
                    WHERE qb.id = :bankId AND q.isActive = true AND q.isDelete = false
            """)
    List<Question> findByQuestionBankId(Long bankId);

    @Query("""
                    SELECT q FROM Question q
                    JOIN QuestionQuiz qq ON q.id = qq.question.id
                    WHERE qq.quiz.id = :idQuiz
            """)
    List<Question> findByQuizId(Long idQuiz);
}
