package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Room;
import com.doan2025.webtoeic.dto.SearchRoomDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("""
                    SELECT r FROM Room r
                    WHERE (COALESCE(:#{#dto.searchString}, null) is null
                            or lower(CAST(r.name as STRING)) LIKE LOWER(CONCAT('%', :#{#dto.searchString},'%'))
                            or lower(CAST(r.description as STRING)) LIKE LOWER(CONCAT('%', :#{#dto.searchString},'%'))
                            )
                            AND (COALESCE(:#{#dto.isActive}, null) is null OR r.isActive = :#{#dto.isActive})
                            AND (COALESCE(:#{#dto.isDelete}, null) is null OR r.isDelete = :#{#dto.isDelete})
            """)
    List<Room> filter(SearchRoomDto dto);
}
