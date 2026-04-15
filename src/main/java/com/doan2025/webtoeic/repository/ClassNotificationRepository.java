package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.ClassNotification;
import com.doan2025.webtoeic.dto.SearchNotificationInClassDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassNotificationRepository extends JpaRepository<ClassNotification, Long> {

    @Query("""
                    SELECT cn FROM ClassNotification cn
                    WHERE cn.clazz.id = :#{#dto.classId}
                        AND (:role != 'STUDENT' OR (cn.isDelete = false AND cn.isActive = true) )
                        AND (COALESCE(:#{#dto.notiTypes}, null) IS NULL OR cn.typeNotification IN (:#{#dto.notiTypes}) )
                         AND (coalesce(:#{#dto.searchString} , null) IS NULL
                                OR LOWER(CAST(cn.description as string ) )  LIKE LOWER(CONCAT('%', :#{#dto.searchString}, '%')))
                          AND ( (COALESCE(:#{#dto.fromDate}, null ) is null AND COALESCE(:#{#dto.toDate}, null ) is null )
                              OR ((COALESCE(:#{#dto.fromDate}, null ) is null AND COALESCE(:#{#dto.toDate}, null ) is not null AND cn.createdAt <= :#{#dto.toDate}))
                              OR ((COALESCE(:#{#dto.fromDate}, null ) is not null AND COALESCE(:#{#dto.toDate}, null ) is null AND cn.createdAt >= :#{#dto.fromDate}))
                              OR cn.createdAt between :#{#dto.fromDate} and :#{#dto.toDate})
            
            """)
    Page<ClassNotification> findByClazzId(SearchNotificationInClassDto dto, String role, Pageable pageable);
}
