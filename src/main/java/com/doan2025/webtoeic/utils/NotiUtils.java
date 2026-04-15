package com.doan2025.webtoeic.utils;

import com.doan2025.webtoeic.constants.enums.ENotiType;
import com.doan2025.webtoeic.domain.Notification;
import com.doan2025.webtoeic.domain.NotificationReceive;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.repository.NotiRepository;
import com.doan2025.webtoeic.repository.NotificationReceiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotiUtils {

    private final NotiRepository notiRepository;
    private final NotificationReceiveRepository notificationReceiveRepository;

    public void sendNoti(List<User> users, ENotiType notiType, String title, String content, Long objectId) {
        Notification notification = Notification.builder()
                .content(content)
                .title(title)
                .objectId(objectId)
                .notiType(notiType)
                .build();
        Notification notiSaved = notiRepository.save(notification);
        for (User user : users) {
            NotificationReceive notificationReceive = NotificationReceive.builder()
                    .receiver(user)
                    .notification(notification)
                    .isRead(false)
                    .build();

            notificationReceiveRepository.save(notificationReceive);
        }

    }
}
