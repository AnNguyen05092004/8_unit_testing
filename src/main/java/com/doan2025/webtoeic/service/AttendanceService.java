package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.request.AttendanceRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AttendanceService {


    void updateAttendance(HttpServletRequest httpServletRequest, List<AttendanceRequest> requests);

    void attendance(HttpServletRequest httpServletRequest, List<AttendanceRequest> requests);
}
