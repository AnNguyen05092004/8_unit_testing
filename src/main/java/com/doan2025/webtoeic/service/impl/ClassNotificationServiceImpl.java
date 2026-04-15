package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.*;
import com.doan2025.webtoeic.domain.AttachDocumentClass;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.domain.ClassNotification;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchNotificationInClassDto;
import com.doan2025.webtoeic.dto.request.ClassNotificationRequest;
import com.doan2025.webtoeic.dto.response.ClassNotificationResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.service.ClassNotificationService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.doan2025.webtoeic.utils.NotiUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class ClassNotificationServiceImpl implements ClassNotificationService {
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;
    private final ClassNotificationRepository classNotificationRepository;
    private final AttachDocumentClassRepository attachDocumentClassRepository;
    private final ClassMemberRepository classMemberRepository;
    private final NotiUtils notiUtils;

    @Override
    public ClassNotificationResponse getDetailNotificationInClass(HttpServletRequest httpServletRequest, Long notificationId) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        ClassNotification noti = classNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.NOTIFICATION));

        if (Objects.equals(user.getRole(), ERole.STUDENT)) {
            if (!classMemberRepository.existsMemberInClass(noti.getClazz().getId(), user.getId())) {
                throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
            }
        }

        return convertUtil.convertClassNotificationToDto(httpServletRequest,
                noti,
                attachDocumentClassRepository.findByClassNotificationId(noti.getId()));
    }

    @Override
    public Page<ClassNotificationResponse> getListNotificationInClass(HttpServletRequest httpServletRequest, SearchNotificationInClassDto dto, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        Class clazz = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CLASS));

        if (Objects.equals(user.getRole(), ERole.STUDENT)) {
            if (!classMemberRepository.existsMemberInClass(dto.getClassId(), user.getId())) {
                throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
            }
        }

        Page<ClassNotification> classNotifications = classNotificationRepository.findByClazzId(dto, user.getRole().name(), pageable);

        return classNotifications.map(item ->
                convertUtil.convertClassNotificationToDto(
                        httpServletRequest,
                        item,
                        attachDocumentClassRepository.findByClassNotificationId(item.getId())
                )
        );
    }

    @Override
    public ClassNotificationResponse createNotificationInClass(HttpServletRequest httpServletRequest, ClassNotificationRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        Class clazz = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CLASS));

        if (!Objects.equals(user.getEmail(), clazz.getTeacher().getEmail())
                && !Objects.equals(ERole.CONSULTANT, user.getRole())
                && !Objects.equals(ERole.MANAGER, user.getRole())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
        ClassNotification noti = ClassNotification.builder()
                .description(request.getDescription())
                .isPin(request.getIsPin() != null && request.getIsPin())
                .clazz(clazz)
                .createdBy(user)
                .typeNotification(EClassNotificationType.fromValue(request.getTypeNotification()))
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .build();
        ClassNotification saved = classNotificationRepository.save(noti);
        if (request.getUrlAttachment() != null && !request.getUrlAttachment().isEmpty()) {
            for (String url : request.getUrlAttachment()) {
                AttachDocumentClass attachment = AttachDocumentClass.builder()
                        .linkUrl(url)
                        .classNotification(noti)
                        .build();
                attachDocumentClassRepository.save(attachment);
            }
        }

        List<User> users = classMemberRepository.findMembersInClass(clazz.getId());
        notiUtils.sendNoti(users,
                ENotiType.UPDATE_IN_CLASS,
                Constants.UPDATE_IN_CLASS_CONTENT,
                Constants.UPDATE_IN_CLASS_CONTENT,
                clazz.getId());

        return convertUtil.convertClassNotificationToDto(httpServletRequest,
                saved,
                attachDocumentClassRepository.findByClassNotificationId(noti.getId()));
    }

    @Override
    public ClassNotificationResponse updateNotificationInClass(HttpServletRequest httpServletRequest, ClassNotificationRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        ClassNotification noti = classNotificationRepository.findById(request.getClassNotificationId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.NOTIFICATION));
        if (!Objects.equals(user.getEmail(), noti.getClazz().getTeacher().getEmail())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
        List.of(
                new FieldUpdateUtil<>(noti::getDescription, noti::setDescription, request.getDescription()),
                new FieldUpdateUtil<>(noti::getTypeNotification, noti::setTypeNotification, EClassNotificationType.fromValue(request.getTypeNotification())),
                new FieldUpdateUtil<>(noti::getIsPin, noti::setIsPin, request.getIsPin() != null && request.getIsPin()),
                new FieldUpdateUtil<>(noti::getFromDate, noti::setFromDate, request.getFromDate()),
                new FieldUpdateUtil<>(noti::getToDate, noti::setToDate, request.getToDate())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        if (Objects.nonNull(request.getUrlAttachment())) {
            attachDocumentClassRepository.deleteAllAttachDocumentClassByClassNotificationId(noti.getId());
            for (String url : request.getUrlAttachment()) {
                AttachDocumentClass attachment = AttachDocumentClass.builder()
                        .linkUrl(url)
                        .classNotification(noti)
                        .build();
                attachDocumentClassRepository.save(attachment);
            }
        }

        noti.setUpdatedBy(user);
        return convertUtil.convertClassNotificationToDto(httpServletRequest,
                noti,
                attachDocumentClassRepository.findByClassNotificationId(noti.getId()));
    }

    @Override
    public ClassNotificationResponse disableOrDeleteNotificationInClass(HttpServletRequest httpServletRequest, ClassNotificationRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        ClassNotification noti = classNotificationRepository.findById(request.getClassNotificationId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.NOTIFICATION));
        if (!Objects.equals(user.getEmail(), noti.getClazz().getTeacher().getEmail())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
        if (request.getIsActive() != null && !Objects.equals(request.getIsActive(), noti.getIsActive())) {
            noti.setIsActive(request.getIsActive());
        }
        if (request.getIsDelete() != null && !Objects.equals(request.getIsDelete(), noti.getIsDelete())) {
            noti.setIsDelete(request.getIsDelete());
        }
        noti.setUpdatedBy(user);
        return convertUtil.convertClassNotificationToDto(httpServletRequest,
                noti,
                attachDocumentClassRepository.findByClassNotificationId(noti.getId()));
    }
}
