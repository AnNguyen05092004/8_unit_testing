package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.RangeTopic;
import com.doan2025.webtoeic.dto.SearchRangeTopicAndScoreScaleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RangeTopicRepository extends JpaRepository<RangeTopic, Long> {
    @Query("""
                    SELECT rt FROM RangeTopic rt
                    WHERE COALESCE(:#{#dto.searchString}, NULL) is NULL
                    OR LOWER(CAST(rt.content as string)) LIKE CONCAT('%', :#{#dto.searchString} , '%')
                    OR LOWER(CAST(rt.vietnamese as string)) LIKE CONCAT('%', :#{#dto.searchString} , '%')
                    OR LOWER(CAST(rt.description as string)) LIKE CONCAT('%', :#{#dto.searchString} , '%')
            """)
    Page<RangeTopic> filter(SearchRangeTopicAndScoreScaleDto dto, Pageable pageable);

    @Query("""
                    SELECT rt FROM RangeTopic rt
                    WHERE (LOWER(CAST(rt.content as string))  LIKE LOWER( CONCAT('%', :content,'%') )
                                OR LOWER(CAST(rt.description as string))  LIKE LOWER( CONCAT('%', :content,'%') ))
            """)
    RangeTopic findByContent(String content);
}
