package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.AttachDocumentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachDocumentClassRepository extends JpaRepository<AttachDocumentClass, Long> {

    @Query("""
            SELECT adc.linkUrl FROM AttachDocumentClass adc
            WHERE adc.classNotification.id = :notificationId
            """)
    List<String> findUrlAttachmentByClassNotificationId(Long notificationId);

    void deleteAllAttachDocumentClassByClassNotificationId(Long notificationId);

    List<AttachDocumentClass> findByClassNotificationId(Long classNotificationId);
}
