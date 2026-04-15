package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchScheduleSto;
import com.doan2025.webtoeic.dto.request.ClassScheduleRequest;
import com.doan2025.webtoeic.dto.response.ClassScheduleResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClassScheduleService {

    Page<?> detailStatisticAttendance(HttpServletRequest httpServletRequest, Long scheduleId, Pageable pageable);

    Page<?> overviewStatisticAttendance(HttpServletRequest httpServletRequest, Long classId, Pageable pageable);

    Page<?> overviewStudentAttendance(HttpServletRequest httpServletRequest, Long classId, Pageable pageable);

    ClassScheduleResponse getScheduleDetail(HttpServletRequest request, Long scheduleId);

    Page<?> getClassSchedule(HttpServletRequest request, SearchScheduleSto dto, Pageable pageable);

    List<?> createScheduleInClass(HttpServletRequest request, List<ClassScheduleRequest> classScheduleRequest);

    ClassScheduleResponse updateScheduleInClass(HttpServletRequest request, ClassScheduleRequest classScheduleRequest);

    void cancelledScheduleInClass(HttpServletRequest request, List<Long> idsOfClassSchedule);
}
