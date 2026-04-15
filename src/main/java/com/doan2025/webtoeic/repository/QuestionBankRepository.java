package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.QuestionBank;
import com.doan2025.webtoeic.dto.SearchBankDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {

    @Query("""
                    SELECT qb from QuestionBank qb
                    JOIN qb.createBy u ON qb.createBy.id = u.id
                    WHERE (COALESCE(:#{#dto.searchString}, null) IS NULL
                     OR LOWER(CAST(qb.title as string)) LIKE LOWER( CONCAT('%',:#{#dto.searchString},'%') )
                     )
                     AND (COALESCE(:#{#dto.createByIds}, null) IS NULL OR u.id IN (:#{#dto.createByIds}))
                     AND (COALESCE(:#{#dto.isActive}, null) IS NULL OR qb.isActive = :#{#dto.isActive} )
                     AND (COALESCE(:#{#dto.isDelete}, null) IS NULL OR qb.isDelete = :#{#dto.isDelete} )
            """)
    Page<QuestionBank> filter(SearchBankDto dto, Pageable pageable);
}
