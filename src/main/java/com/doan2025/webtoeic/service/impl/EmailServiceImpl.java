package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String FROM_MAIL;

    @Override
    @Retryable(value = MailException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_MAIL);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException e) {
            throw new WebToeicException(ResponseCode.CANNOT_SEND, ResponseObject.EMAIL);
        }
    }

    @Override
    @Retryable(value = MailException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendClassNotification(Set<String> recipients, String subject, String content) {
        // 1. Kiểm tra danh sách nhận
        if (recipients == null || recipients.isEmpty()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(FROM_MAIL);
            message.setSubject(subject);
            message.setText(content);

            // 2. Kỹ thuật quan trọng:
            // Set trường "To" là email hệ thống (hoặc chính sender) để tránh lỗi SMTP khi "To" bị trống.
            message.setTo(FROM_MAIL);

            // 3. Đưa toàn bộ danh sách người nhận vào BCC
            // Để các thành viên không nhìn thấy email của nhau.
            String[] bccArray = recipients.toArray(new String[0]);
            message.setBcc(bccArray);

            // 4. Gửi mail
            mailSender.send(message);

        } catch (MailException e) {
        }
    }
}
