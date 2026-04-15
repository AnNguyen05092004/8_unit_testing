package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Answer;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.request.AnswerRequest;
import com.doan2025.webtoeic.dto.response.AnswerResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.AnswerRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.AnswerService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class, WebToeicException.class})
public class AnswerServiceImpl implements AnswerService {
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;


    @Override
    public AnswerResponse getAnswer(HttpServletRequest httpServletRequest, Long id) {
        return null;
    }

    @Override
    public AnswerResponse getAnswerList(HttpServletRequest httpServletRequest, Long idQuestion) {
        return null;
    }

    @Override
    public AnswerResponse saveAnswer(HttpServletRequest httpServletRequest, AnswerRequest answerRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        Answer answer = Answer.builder()
                .content(answerRequest.getContent())
                .isCorrect(answerRequest.getCorrect())
                .createdBy(user)
                .build();
        Answer savedAnswer = answerRepository.save(answer);
        return convertUtil.convertAnswerToDto(savedAnswer);
    }

    @Override
    public AnswerResponse updateAnswer(HttpServletRequest httpServletRequest, AnswerRequest answerRequest) {
        Answer answer = answerRepository.findById(answerRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ANSWER));

        List.of(
                new FieldUpdateUtil<>(answer::getContent, answer::setContent, answerRequest.getContent()),
                new FieldUpdateUtil<>(answer::getIsCorrect, answer::setIsCorrect, answerRequest.getCorrect())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        answer.setUpdatedBy(user);

        Answer saved = answerRepository.save(answer);
        return convertUtil.convertAnswerToDto(saved);
    }

    @Override
    public AnswerResponse deleteAnswer(HttpServletRequest httpServletRequest, Long id) {
        return null;
    }


}
