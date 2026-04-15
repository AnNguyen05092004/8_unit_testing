package com.doan2025.webtoeic.config;

import com.doan2025.webtoeic.domain.ClassSchedule;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.repository.ClassMemberRepository;
import com.doan2025.webtoeic.repository.ClassScheduleRepository;
import com.doan2025.webtoeic.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ScheduleConfig {
    private final EmailService emailService;
    private final ClassMemberRepository classMemberRepository;
    private final ClassScheduleRepository classScheduleRepository;

    @Scheduled(cron = "0 0 21 * * *")
    @Transactional(readOnly = true)
    public void notifyUpcomingClasses() {

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Date startOfTomorrow = Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfTomorrow = Date.from(tomorrow.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        List<ClassSchedule> schedules = classScheduleRepository.findSchedulesForNextDay(startOfTomorrow, endOfTomorrow);

        if (schedules.isEmpty()) {
            return;
        }

        // 3. Duyệt qua từng lịch học để gửi mail
        for (ClassSchedule schedule : schedules) {
            try {
                processNotificationForSchedule(schedule);
            } catch (Exception e) {
            }
        }
    }

    private void processNotificationForSchedule(ClassSchedule schedule) {
        // Lấy thông tin lớp học
        var clazz = schedule.getClazz();
        if (clazz == null) return;

        Set<String> recipientEmails = new HashSet<>();

        // A. Thêm email Giáo viên chủ nhiệm (từ bảng Class)
        if (clazz.getTeacher() != null && clazz.getTeacher().getEmail() != null) {
            recipientEmails.add(clazz.getTeacher().getEmail());
        }

        // B. Thêm email Học sinh/Thành viên (từ bảng ClassMember)
        List<User> members = classMemberRepository.findMembersInClass(clazz.getId());

        for (User user : members) {
            if (user != null && user.getEmail() != null) {
                recipientEmails.add(user.getEmail());
            }
        }

        if (recipientEmails.isEmpty()) return;

        // C. Soạn nội dung email
        String subject = "[Thông báo lịch học] " + clazz.getName() + " - " + schedule.getTitle();
        String content = buildEmailContent(schedule, clazz);

        // D. Gửi email
        emailService.sendClassNotification(recipientEmails, subject, content);
    }

    private String buildEmailContent(ClassSchedule schedule, com.doan2025.webtoeic.domain.Class clazz) {
        // Có thể dùng HTML template engine như Thymeleaf ở đây để đẹp hơn
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào,\n\n");
        sb.append("Đây là email nhắc nhở về buổi học ngày mai của bạn:\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Lớp học: ").append(clazz.getName()).append("\n");
        sb.append("Nội dung: ").append(schedule.getTitle()).append("\n");
        sb.append("Thời gian: ").append(formatTime(schedule.getStartAt())).append(" - ").append(formatTime(schedule.getEndAt())).append("\n");

        if (schedule.getRoom() != null) {
            sb.append("Phòng học: ").append(schedule.getRoom().getName()).append("\n"); // Giả sử Room có getName()
        }

        sb.append("Giáo viên: ").append(clazz.getTeacher() != null ? clazz.getTeacher().getFirstName() + " " + clazz.getTeacher().getLastName() : "N/A").append("\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Vui lòng đến đúng giờ.\n\n");
        sb.append("Trân trọng,\nĐội ngũ quản trị.");

        return sb.toString();
    }

    private String formatTime(Date date) {
        if (date == null) return "";
        return date.toString();
    }

}
