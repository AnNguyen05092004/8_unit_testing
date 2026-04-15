package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Lesson;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.response.LessonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Query("""
            SELECT new com.doan2025.webtoeic.dto.response.LessonResponse(
                l.id, l.title, l.content, l.videoUrl, l.duration, l.orderIndex,
                l.isPreviewAble, l.isDelete, l.isActive, l.createdAt, l.updatedAt,
                CONCAT(cb.firstName, ' ', cb.lastName), CONCAT(ub.firstName, ' ', ub.lastName),
                CASE
                    WHEN EXISTS (
                        SELECT 1 FROM Enrollment e
                        WHERE e.course.id = l.course.id AND e.user.email = :email
                    ) THEN TRUE
                    ELSE FALSE
                END )
            FROM Lesson l
            LEFT JOIN l.createdBy cb
            LEFT JOIN l.updatedBy ub
            WHERE l.course.id = :#{#dto.id}
                  AND (COALESCE(:#{#dto.isActive}, null) is null or l.isActive = :#{#dto.isActive} )
                  AND (COALESCE(:#{#dto.isDelete}, null) is null or l.isDelete = :#{#dto.isDelete} )
            ORDER BY l.orderIndex asc
            """)
    Page<LessonResponse> findLessons(SearchBaseDto dto, String email, Pageable pageable);

    @Query("""
            SELECT new com.doan2025.webtoeic.dto.response.LessonResponse(
                        l.id, l.title, l.content, l.videoUrl, l.duration, l.orderIndex,
                        l.isPreviewAble, l.isDelete, l.isActive, l.createdAt, l.updatedAt,
                        CONCAT(cb.firstName, ' ', cb.lastName), CONCAT(ub.firstName, ' ', ub.lastName)
                        )
            FROM Lesson l
            LEFT JOIN l.createdBy cb
            LEFT JOIN l.updatedBy ub
            WHERE cb.email = :email AND l.isDelete = FALSE
                  AND ( COALESCE(:#{#dto.title}, null) is null OR  LOWER(cast(l.title as string)) LIKE LOWER(CONCAT('%', :#{#dto.title}, '%')) )
                  AND ( (COALESCE(:#{#dto.fromDate}, null ) is null AND COALESCE(:#{#dto.toDate}, null ) is null )
                        OR l.createdAt between :#{#dto.fromDate} and :#{#dto.toDate})
            """)
    Page<LessonResponse> findOwnLessons(SearchBaseDto dto, String email, Pageable pageable);

    @Query("""
            SELECT new com.doan2025.webtoeic.dto.response.LessonResponse(
                        l.id, l.title, l.content, l.videoUrl, l.duration, l.orderIndex,
                        l.isPreviewAble, l.isDelete, l.isActive, l.createdAt, l.updatedAt,
                        CONCAT(cb.firstName, ' ', cb.lastName), CONCAT(ub.firstName, ' ', ub.lastName)
                        )
            FROM Lesson l
            LEFT JOIN l.createdBy cb
            LEFT JOIN l.updatedBy ub
            WHERE ( COALESCE(:#{#dto.title}, null) is null OR  LOWER(cast(l.title as string))  LIKE LOWER(CONCAT('%', :#{#dto.title}, '%')) )
                  AND ( (COALESCE(:#{#dto.fromDate}, null ) is null AND COALESCE(:#{#dto.toDate}, null ) is null )
                        OR l.createdAt between :#{#dto.fromDate} and :#{#dto.toDate})
                  AND (COALESCE(:#{#dto.isActive}, NULL) IS NULL OR l.isActive = :#{#dto.isActive} )
                  AND (COALESCE(:#{#dto.isDelete}, NULL) IS NULL OR l.isDelete = :#{#dto.isDelete} )
            """)
    Page<LessonResponse> findAllLessons(SearchBaseDto dto, Pageable pageable);
}
