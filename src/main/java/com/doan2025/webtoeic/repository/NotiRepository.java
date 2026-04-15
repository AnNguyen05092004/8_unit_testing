package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.Notification;
import com.doan2025.webtoeic.dto.response.NotiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotiRepository extends JpaRepository<Notification, Long> {

    @Query("""
                    SELECT COUNT(n) from Notification n
                    JOIN NotificationReceive nr ON n.id = nr.notification.id
                    WHERE nr.receiver.id = ?1 AND nr.isRead = false
            """)
    Long countNotiByReceiverId(Long receiverId);


    @Query("""
                    SELECT new com.doan2025.webtoeic.dto.response.NotiResponse(
                    n.id, n.title, n.content, n.objectId, n.notiType,n.createdAt, nr.isRead
                    )
                    from Notification n
                    JOIN NotificationReceive nr ON n.id = nr.notification.id
                   WHERE nr.receiver.id = :receiverId
            """)
    Page<NotiResponse> filter(Long receiverId, Pageable pageable);
}
