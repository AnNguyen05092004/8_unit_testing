package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.dto.SearchClassDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    @Query("""
            SELECT DISTINCT c
            FROM Class c
            WHERE (coalesce(:#{#dto.searchString}, NULL) is NULL
                        OR LOWER(cast(c.title as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%'))
                        OR LOWER(cast(c.description as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%'))
                        OR LOWER(cast(c.subject as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%'))
                        OR LOWER(cast(c.name as string))  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%')) )
                AND ( (COALESCE(:#{#dto.fromDate}, null ) is null AND COALESCE(:#{#dto.toDate}, null ) is null )
                    OR c.createdAt between :#{#dto.fromDate} and :#{#dto.toDate})
                AND (COALESCE(:#{#dto.idTeacher}, NULL) IS NULL OR c.teacher.id = :#{#dto.idTeacher} )
                AND (COALESCE(:#{#dto.statusClass}, NULL) IS NULL OR c.status IN (:#{#dto.statusClass}))
                AND (COALESCE(:classIds, NULL) IS NULL OR c.id IN (:classIds) )
            """)
    Page<Class> filterClass(SearchClassDto dto, List<Long> classIds, Pageable pageable);
}
