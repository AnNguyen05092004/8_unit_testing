package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.NotificationReceive;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.response.NotiResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.NotiRepository;
import com.doan2025.webtoeic.repository.NotificationReceiveRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.NotiService;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class NotiServiceImpl implements NotiService {
    private final NotiRepository notiRepository;
    private final NotificationReceiveRepository notificationReceiveRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void sendNoti(HttpServletRequest request) {

    }

    @Override
    public Long countNoti(HttpServletRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        return notiRepository.countNotiByReceiverId(user.getId());
    }

    @Override
    public Page<NotiResponse> listNoti(HttpServletRequest request, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        return notiRepository.filter(user.getId(), pageable);
    }

    @Override
    public void updateNoti(HttpServletRequest request, List<Long> notiIds) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        for (Long notiId : notiIds) {
            NotificationReceive notificationReceive = notificationReceiveRepository.findByNotificationIdAndReceiverId(notiId, user.getId());
            notificationReceive.setIsRead(Boolean.TRUE);
            notificationReceiveRepository.save(notificationReceive);
        }
    }
}
