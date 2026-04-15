package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.ScoreScale;
import com.doan2025.webtoeic.dto.SearchRangeTopicAndScoreScaleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreScaleRepository extends JpaRepository<ScoreScale, Long> {
    @Query("""
            SELECT ss FROM ScoreScale ss
            WHERE COALESCE(:#{#dto.searchString}, NULL) is NULL
            OR LOWER(CAST(ss.title as string)) LIKE CONCAT('%', :#{#dto.searchString} , '%')
            """)
    Page<ScoreScale> filter(SearchRangeTopicAndScoreScaleDto dto, Pageable pageable);

    @Query("""
                    SELECT ss FROM ScoreScale ss
                    WHERE (LOWER(CAST(ss.title as string))  LIKE LOWER(:title))
            """)
    ScoreScale findByTitle(String title);
}
