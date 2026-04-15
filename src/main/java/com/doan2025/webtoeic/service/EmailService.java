package com.doan2025.webtoeic.service;

import java.util.Set;

public interface EmailService {
    void sendEmail(String toEmail, String subject, String body);

    void sendClassNotification(Set<String> recipients, String subject, String content);
}
