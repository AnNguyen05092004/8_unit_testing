package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.SubmitExercise;
import com.doan2025.webtoeic.dto.SearchSubmitExerciseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmitExerciseRepository extends JpaRepository<SubmitExercise, Long> {

    @Query("""
                     SELECT se FROM SubmitExercise se
                     JOIN se.createdBy uc
                     WHERE se.classNotification.id = :#{#dto.notificationId}
                        AND (COALESCE(:email, null) is null OR :email = uc.email)
                        AND (COALESCE(:#{#dto.searchString}, null) IS NULL
                            OR LOWER(CAST(CONCAT(uc.firstName, ' ', uc.lastName) as string)) LIKE CONCAT('%', :#{#dto.searchString}, '%') )
            """)
    Page<SubmitExercise> findByClassNotificationId(SearchSubmitExerciseDto dto, Pageable pageable, String email);

    List<SubmitExercise> findByClassNotificationIdAndCreatedById(Long notificationId, Long id);
}
