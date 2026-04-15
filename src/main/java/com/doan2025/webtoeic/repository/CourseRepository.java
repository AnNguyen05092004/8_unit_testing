package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.response.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("""
            SELECT DISTINCT new com.doan2025.webtoeic.dto.response.CourseResponse(
            c.id, c.title, c.description, c.price, c.thumbnailUrl, c.categoryCourse,
            c.updatedAt, c.createdAt, c.isDelete, c.isActive, CONCAT(a.firstName, ' ', a.lastName ) ,
            CONCAT(cb.firstName, ' ', cb.lastName ) , CONCAT(ub.firstName, ' ', ub.lastName ),
            CASE
                WHEN EXISTS (
                    SELECT 1 FROM Enrollment e
                    WHERE e.course.id = c.id AND e.user.email = :email
                ) THEN TRUE
                ELSE FALSE
            END ,
            CASE
                WHEN EXISTS (
                    SELECT 1 FROM Orders o
                    JOIN OrderDetail od ON o.id = od.orders.id
                    WHERE od.course.id = c.id AND o.user.email = :email
                ) THEN (
                   SELECT MIN(o.id) FROM Orders o
                   JOIN OrderDetail od ON o.id = od.orders.id
                   WHERE od.course.id = c.id AND o.user.email = :email
                 )
                ELSE NULL
            END,
            CASE
                WHEN EXISTS (
                    SELECT 1 FROM Orders o
                    JOIN OrderDetail od ON o.id = od.orders.id
                    WHERE od.course.id = c.id AND o.user.email = :email
                ) THEN (
                   SELECT CAST(o_main.status as string)
                   FROM Orders o_main
                   WHERE o_main.id = (
                       SELECT MIN(o_sub.id)
                       FROM Orders o_sub
                       JOIN OrderDetail od_sub ON o_sub.id = od_sub.orders.id
                       WHERE od_sub.course.id = c.id AND o_sub.user.email = :email
                   )
                 )
                ELSE NULL
            END
            )
            FROM Course c
            LEFT JOIN c.author a
            left JOIN c.createdBy cb
            LEFT JOIN c.updatedBy ub
            WHERE c.isDelete = false AND c.isActive = TRUE
                AND ( COALESCE(:#{#dto.title}, null) is null OR LOWER(cast( c.title as string)) LIKE LOWER(CONCAT('%', :#{#dto.title}, '%'))  )
                AND ( (COALESCE(:#{#dto.fromDate}, null ) is null AND COALESCE(:#{#dto.toDate}, null ) is null )
                        OR c.createdAt between :#{#dto.fromDate} and :#{#dto.toDate})
                AND (COALESCE(:#{#dto.categories}, null) is null OR c.categoryCourse IN (:#{#dto.categories}) )
            """)
    Page<CourseResponse> findCourses(SearchBaseDto dto, String email, Pageable pageable);

    @Query("""
            SELECT DISTINCT new com.doan2025.webtoeic.dto.response.CourseResponse(
            c.id, c.title, c.description, c.price, c.thumbnailUrl, c.categoryCourse,
            c.updatedAt, c.createdAt, c.isDelete, c.isActive, CONCAT(a.firstName, ' ', a.lastName ) ,
            CONCAT(cb.firstName, ' ', cb.lastName ) , CONCAT(ub.firstName, ' ', ub.lastName ) )
            FROM Course c
            LEFT JOIN c.author a
            left JOIN c.lessons l
            left JOIN c.createdBy cb
            left JOIN c.updatedBy ub
            WHERE ( COALESCE(:#{#dto.title}, null) is null OR LOWER(cast( c.title as string)) LIKE LOWER(CONCAT('%', :#{#dto.title}, '%'))  )
                AND ( (COALESCE(:#{#dto.fromDate}, null ) is null AND COALESCE(:#{#dto.toDate}, null ) is null )
                        OR (
                            CAST(c.createdAt AS DATE) >= CAST(:#{#dto.fromDate} AS DATE)
                            AND
                            CAST(c.createdAt AS DATE) <= CAST(:#{#dto.toDate} AS DATE)
                        ))
                AND (COALESCE(:#{#dto.categories}, null) is null OR c.categoryCourse IN (:#{#dto.categories}) )
                AND (COALESCE(:#{#dto.isActive}, NULL) IS NULL OR c.isActive = :#{#dto.isActive} )
                AND (COALESCE(:#{#dto.isDelete}, NULL) IS NULL OR c.isDelete = :#{#dto.isDelete} )
            """)
    Page<CourseResponse> findAllCourses(SearchBaseDto dto, Pageable pageable);

    @Query("""
            SELECT DISTINCT new com.doan2025.webtoeic.dto.response.CourseResponse(
            c.id, c.title, c.description, c.price, c.thumbnailUrl, c.categoryCourse,
            c.updatedAt, c.createdAt, c.isDelete, c.isActive, CONCAT(a.firstName, ' ', a.lastName ) ,
            CONCAT(cb.firstName, ' ', cb.lastName ) , CONCAT(ub.firstName, ' ', ub.lastName ) )
            FROM Course c
            LEFT JOIN c.author a
            left JOIN c.lessons l
            left JOIN c.createdBy cb
            left JOIN c.updatedBy ub
            WHERE c.isDelete = false
                AND c.createdBy.email = :email
                AND ( COALESCE(:#{#dto.title}, null) is null OR LOWER(cast( c.title as string)) LIKE LOWER(CONCAT('%', :#{#dto.title}, '%'))  )
                AND ( (COALESCE(:#{#dto.fromDate}, null ) is null AND COALESCE(:#{#dto.toDate}, null ) is null )
                        OR c.createdAt between :#{#dto.fromDate} and :#{#dto.toDate})
                AND (COALESCE(:#{#dto.categories}, null) is null OR c.categoryCourse IN (:#{#dto.categories}) )
            """)
    Page<CourseResponse> findOwnCourses(SearchBaseDto dto, String email, Pageable pageable);

}
