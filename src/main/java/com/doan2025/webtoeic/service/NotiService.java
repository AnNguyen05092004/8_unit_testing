package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.response.NotiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotiService {

    void sendNoti(HttpServletRequest request);

    Long countNoti(HttpServletRequest request);

    Page<NotiResponse> listNoti(HttpServletRequest request, Pageable pageable);

    void updateNoti(HttpServletRequest request, List<Long> notiId);
}
