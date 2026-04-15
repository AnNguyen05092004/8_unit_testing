package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.ClassNotification;
import com.doan2025.webtoeic.domain.SubmitExercise;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchSubmitExerciseDto;
import com.doan2025.webtoeic.dto.request.SubmitExerciseRequest;
import com.doan2025.webtoeic.dto.response.SubmitExerciseResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.ClassMemberRepository;
import com.doan2025.webtoeic.repository.ClassNotificationRepository;
import com.doan2025.webtoeic.repository.SubmitExerciseRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.SubmitExersiceService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class SubmitExerciseServiceImpl implements SubmitExersiceService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;
    private final ClassNotificationRepository classNotificationRepository;
    private final ClassMemberRepository classMemberRepository;
    private final SubmitExerciseRepository submitExerciseRepository;

    @Override
    public SubmitExerciseResponse getDetailSubmitExercise(HttpServletRequest httpServletRequest, Long submitId) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        SubmitExercise submitExercise = submitExerciseRepository.findById(submitId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SUBMIT));

        if (Objects.equals(user.getRole(), ERole.TEACHER)
                && classMemberRepository.existsMemberInClass(submitExercise.getClassNotification().getClazz().getId(), user.getId())
                || Objects.equals(user.getId(), submitExercise.getCreatedBy().getId())) {
            return convertUtil.convertSubmitExerciseToDto(httpServletRequest, submitExercise);
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
    }

    @Override
    public Page<SubmitExerciseResponse> getListSubmitExercise(HttpServletRequest httpServletRequest, SearchSubmitExerciseDto dto, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        ClassNotification noti = classNotificationRepository.findById(dto.getNotificationId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.NOTIFICATION));

        if (classMemberRepository.existsMemberInClass(noti.getClazz().getId(), user.getId())) {
            Page<SubmitExercise> pages;
            if (Objects.equals(user.getRole(), ERole.TEACHER)) {
                pages = submitExerciseRepository.findByClassNotificationId(dto, pageable, null);
            } else {
                pages = submitExerciseRepository.findByClassNotificationId(dto, pageable, user.getEmail());
            }
            return pages.map(submitExercise
                    -> convertUtil.convertSubmitExerciseToDto(httpServletRequest, submitExercise));
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
    }

    @Override
    public SubmitExerciseResponse createSubmitExercise(HttpServletRequest httpServletRequest, SubmitExerciseRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        ClassNotification noti = classNotificationRepository.findById(request.getNotificationId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.NOTIFICATION));

        if (!classMemberRepository.existsMemberInClass(noti.getClazz().getId(), user.getId())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }

        Date now = new Date();
        if (now.before(noti.getFromDate())) {
            throw new WebToeicException(ResponseCode.NOT_START, ResponseObject.SUBMIT);
        } else if (now.after(noti.getToDate())) {
            throw new WebToeicException(ResponseCode.OVER_DUE, ResponseObject.SUBMIT);
        }
        List<SubmitExercise> submitExercisePre = submitExerciseRepository.findByClassNotificationIdAndCreatedById(request.getNotificationId(), user.getId());

        if (Objects.nonNull(submitExercisePre)) {
            for (SubmitExercise submitExercise : submitExercisePre) {
                submitExercise.setIsActive(false);
                submitExerciseRepository.save(submitExercise);
            }
        }

        SubmitExercise submitExercise = SubmitExercise.builder()
                .linkUrl(request.getLinkUrl())
                .classNotification(noti)
                .createdBy(user)
                .build();
        SubmitExercise saved = submitExerciseRepository.save(submitExercise);
        return convertUtil.convertSubmitExerciseToDto(httpServletRequest, saved);
    }

    @Override
    public SubmitExerciseResponse updateSubmitExercise(HttpServletRequest httpServletRequest, SubmitExerciseRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        SubmitExercise submitExercise = submitExerciseRepository.findById(request.getSubmitId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.SUBMIT));

        if (!Objects.equals(user.getId(), submitExercise.getCreatedBy().getId())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }

        Date now = new Date();
        if (now.before(submitExercise.getClassNotification().getFromDate())) {
            throw new WebToeicException(ResponseCode.NOT_START, ResponseObject.SUBMIT);
        } else if (now.after(submitExercise.getClassNotification().getToDate())) {
            throw new WebToeicException(ResponseCode.OVER_DUE, ResponseObject.SUBMIT);
        }

        List.of(
                new FieldUpdateUtil<>(submitExercise::getLinkUrl, submitExercise::setLinkUrl, request.getLinkUrl())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        SubmitExercise saved = submitExerciseRepository.save(submitExercise);
        return convertUtil.convertSubmitExerciseToDto(httpServletRequest, saved);
    }

    @Override
    public SubmitExerciseResponse deleteOrCancelSubmitExercise(HttpServletRequest httpServletRequest, SubmitExerciseRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        SubmitExercise submitExercise = submitExerciseRepository.findById(request.getSubmitId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.SUBMIT));

        if (!Objects.equals(user.getId(), submitExercise.getCreatedBy().getId())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
        Date now = new Date();
        if (now.before(submitExercise.getClassNotification().getFromDate())) {
            throw new WebToeicException(ResponseCode.NOT_START, ResponseObject.SUBMIT);
        } else if (now.after(submitExercise.getClassNotification().getToDate())) {
            throw new WebToeicException(ResponseCode.OVER_DUE, ResponseObject.SUBMIT);
        }
        if (request.getIsActive() != null && !Objects.equals(request.getIsActive(), submitExercise.getIsActive())) {
            submitExercise.setIsActive(request.getIsActive());
        }
        if (request.getIsDelete() != null && !Objects.equals(request.getIsDelete(), submitExercise.getIsDelete())) {
            submitExercise.setIsDelete(request.getIsDelete());
        }
        SubmitExercise saved = submitExerciseRepository.save(submitExercise);
        return convertUtil.convertSubmitExerciseToDto(httpServletRequest, saved);
    }
}
