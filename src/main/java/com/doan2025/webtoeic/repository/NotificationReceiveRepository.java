package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.NotificationReceive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationReceiveRepository extends JpaRepository<NotificationReceive, Long> {

    @Query("""
                    SELECT nr FROM NotificationReceive nr
                    WHERE nr.receiver.id = :receiverId AND nr.notification.id = :notiId
            """)
    NotificationReceive findByNotificationIdAndReceiverId(Long notiId, Long receiverId);
}
