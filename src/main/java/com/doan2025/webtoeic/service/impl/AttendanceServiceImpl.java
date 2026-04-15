package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.EAttendanceStatus;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Attendance;
import com.doan2025.webtoeic.domain.ClassSchedule;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.request.AttendanceRequest;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.AttendanceRepository;
import com.doan2025.webtoeic.repository.ClassScheduleRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.AttendanceService;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = {WebToeicException.class, Exception.class})
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    private final ClassScheduleRepository classScheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void updateAttendance(HttpServletRequest httpServletRequest, List<AttendanceRequest> requests) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        if (Objects.isNull(requests.get(0).getScheduleId())) {
            throw new WebToeicException(ResponseCode.IS_NULL, ResponseObject.SCHEDULE);
        }

        ClassSchedule schedule = classScheduleRepository.findById(requests.get(0).getScheduleId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SCHEDULE));

        if (!Objects.equals(user.getRole(), ERole.TEACHER) &&
                !Objects.equals(user.getEmail(), schedule.getClazz().getTeacher().getEmail())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }

        Instant now = Instant.now();
        Instant start = schedule.getStartAt().toInstant();
        Instant end = schedule.getEndAt().toInstant();

        // Nếu now trước (start - 15p)
        if (now.isBefore(start.minus(Duration.ofMinutes(15)))) {
            throw new WebToeicException(ResponseCode.NOT_START, ResponseObject.SCHEDULE);
        }

        // Nếu now sau (end + 15p)
        if (now.isAfter(end.plus(Duration.ofMinutes(15)))) {
            throw new WebToeicException(ResponseCode.OVER_DUE, ResponseObject.SCHEDULE);
        }
        List<Attendance> attendances = new ArrayList<>();
        for (AttendanceRequest request : requests) {
            Attendance attendance = attendanceRepository.findById(request.getAttendanceId())
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ATTENDANCE));
            List.of(
                    new FieldUpdateUtil<>(attendance::getStatus, attendance::setStatus, EAttendanceStatus.fromValue(request.getAttendanceStatus()))
            ).forEach(FieldUpdateUtil::updateIfNeeded);
            attendance.setUpdatedBy(user);
            attendances.add(attendance);
        }
        attendanceRepository.saveAll(attendances);
    }

    @Override
    public void attendance(HttpServletRequest httpServletRequest, List<AttendanceRequest> requests) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        List<Long> scheduleIds = classScheduleRepository.getAvailableSchedule(requests.get(0).getClassId());
        if (Objects.isNull(scheduleIds)) {
            throw new WebToeicException(ResponseCode.NOT_AVAILABLE, ResponseObject.ATTENDANCE);
        }
        List<ClassSchedule> schedules = classScheduleRepository.findAllById(scheduleIds);
        long currentTime = System.currentTimeMillis();
        schedules.sort(Comparator.comparing(ClassSchedule::getEndAt));
        ClassSchedule selectedSchedule = schedules.stream()
                .filter(s -> s.getEndAt() != null && s.getEndAt().getTime() >= currentTime)
                .findFirst()
                .orElse(schedules.get(schedules.size() - 1));

        List<Long> isAttendance = attendanceRepository.findByScheduleId(selectedSchedule.getId());
        if (!Objects.isNull(isAttendance)) {
            throw new WebToeicException(ResponseCode.EXISTED, ResponseObject.ATTENDANCE);
        }
        ClassSchedule schedule = classScheduleRepository.findById(selectedSchedule.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SCHEDULE));

        if (schedule.getIsAttendance()) {
            throw new WebToeicException(ResponseCode.EXISTED, ResponseObject.ATTENDANCE);
        }

        if (!Objects.equals(user.getRole(), ERole.TEACHER) ||
                !Objects.equals(user.getEmail(), schedule.getClazz().getTeacher().getEmail())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }

        List<Attendance> attendances = new ArrayList<>();
        for (AttendanceRequest request : requests) {
            User student = userRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
            Attendance attendance = Attendance.builder()
                    .checkIn(request.getCheckIn())
                    .student(student)
                    .schedule(schedule)
                    .status(EAttendanceStatus.fromValue(request.getAttendanceStatus()))
                    .createdBy(user)
                    .build();
            attendances.add(attendance);
        }
        attendanceRepository.saveAll(attendances);

    }
}
