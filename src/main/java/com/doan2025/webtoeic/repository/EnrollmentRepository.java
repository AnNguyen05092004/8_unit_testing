package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.domain.Enrollment;
import com.doan2025.webtoeic.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserAndCourse(User user, Course course);

    @Query(value = """
                    SELECT c
                    FROM Enrollment e
                    JOIN e.course c
                    WHERE e.user = :user
            """)
    Page<Course> findCourseByUser(User user, Pageable pageable);
}
