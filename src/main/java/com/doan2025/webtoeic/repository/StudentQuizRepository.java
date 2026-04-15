package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.StudentQuiz;
import com.doan2025.webtoeic.dto.SearchSubmittedDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentQuizRepository extends JpaRepository<StudentQuiz, Long> {

    @Query("""
                    SELECT sq FROM StudentQuiz sq
                    JOIN sq.quiz q ON q.id = sq.quiz.id
                    JOIN sq.user u ON u.id = sq.user.id
                    WHERE sq.quiz.id = :idQuiz AND sq.clazz.id = :idClass
                        AND (
                            COALESCE(:#{#dto.searchString}, null) is null
                            OR LOWER(CAST( q.title as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%') )
                            OR LOWER(CAST( q.description as string))  LIKE LOWER(Concat('%', :#{#dto.searchString}, '%'))
                            OR LOWER(CAST(concat(u.firstName, ' ', u.lastName)  as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%') )
                        )
                        AND (COALESCE(:#{#dto.fromScore}, null) is null OR sq.score >= :#{#dto.fromScore})
                        AND (COALESCE(:#{#dto.toScore}, null) is null OR sq.score <= :#{#dto.toScore})
                        AND (COALESCE(:email, null) is null OR :email = u.email)
            """)
    Page<StudentQuiz> filter(Long idQuiz, Long idClass, SearchSubmittedDto dto, Pageable pageable, String email);

    @Query("""
                    SELECT sq FROM StudentQuiz sq
                    JOIN sq.quiz q ON q.id = sq.quiz.id
                    JOIN sq.user u ON u.id = sq.user.id
                    JOIN SharedQuiz shq ON q.id = shq.quiz.id
                    WHERE q.id = :idQuiz AND shq.clazz.id = :idClass
                        AND ( COALESCE(:#{#dto.searchString}, null) is null
                        OR LOWER(CAST( q.title as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%') )
                        OR LOWER(CAST( q.description as string))  LIKE LOWER(Concat('%', :#{#dto.searchString}, '%'))
                        OR LOWER(CAST(concat(u.firstName, ' ', u.lastName)  as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%') )
                        )
                        AND (COALESCE(:#{#dto.fromScore}, null) is null OR sq.score >= :#{#dto.fromScore})
                        AND (COALESCE(:#{#dto.toScore}, null) is null OR sq.score <= :#{#dto.toScore})
            """)
    Page<StudentQuiz> filter(Long idQuiz, SearchSubmittedDto dto, Pageable pageable);

    @Query("""
                    SELECT count(sq) FROM StudentQuiz sq
                    JOIN sq.quiz q ON q.id = sq.quiz.id
                    JOIN sq.user u ON u.id = sq.user.id
                    WHERE q.id = :idSubmitted AND sq.score >= :score
                        AND ( COALESCE(:#{#dto.searchString}, null) is null
                        OR LOWER(CAST( q.title as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%') )
                        OR LOWER(CAST( q.description as string))  LIKE LOWER(Concat('%', :#{#dto.searchString}, '%'))
                        OR LOWER(CAST(concat(u.firstName, ' ', u.lastName)  as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%') )
                        )
                        AND (COALESCE(:#{#dto.fromScore}, null) is null OR sq.score >= :#{#dto.fromScore})
                        AND (COALESCE(:#{#dto.toScore}, null) is null OR sq.score <= :#{#dto.toScore})
            """)
    Long countOver(Long idSubmitted, SearchSubmittedDto dto, Long score);

    @Query("""
                    SELECT sq FROM StudentQuiz sq
                    WHERE sq.user.id = :userId AND sq.clazz.id = :clazzId
                    ORDER BY sq.startAt ASC
            """)
    List<StudentQuiz> findByUser_idAndClazz_id(Long userId, Long clazzId);
}
