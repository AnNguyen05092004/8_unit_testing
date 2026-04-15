package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.ExplanationQuestion;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.request.ExplanationQuestionRequest;
import com.doan2025.webtoeic.dto.response.ExplanationQuestionResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.ExplanationQuestionRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.ExplanationQuestionService;
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
public class ExplanationQuestionServiceImpl implements ExplanationQuestionService {
    private final ExplanationQuestionRepository explanationQuestionRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;

    @Override
    public ExplanationQuestionResponse getExplanationQuestion(HttpServletRequest httpServletRequest, Long id) {
        return null;
    }

    @Override
    public List<ExplanationQuestionResponse> getExplanationQuestions(HttpServletRequest httpServletRequest, Long id) {
        return List.of();
    }

    @Override
    public ExplanationQuestionResponse saveExplanationQuestion(HttpServletRequest httpServletRequest, ExplanationQuestionRequest explanationQuestion) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        ExplanationQuestion exp = ExplanationQuestion.builder()
                .explanationEnglish(explanationQuestion.getExplanationEnglish())
                .explanationVietnamese(explanationQuestion.getExplanationVietnamese())
                .build();
        ExplanationQuestion saved = explanationQuestionRepository.save(exp);
        return convertUtil.convertExplanationQuestionToDto(saved);
    }

    @Override
    public ExplanationQuestionResponse updateExplanationQuestion(HttpServletRequest httpServletRequest, ExplanationQuestionRequest explanationQuestion) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        ExplanationQuestion exp = explanationQuestionRepository.findById(explanationQuestion.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.EXPLANATION));

        List.of(
                new FieldUpdateUtil<>(exp::getExplanationEnglish, exp::setExplanationEnglish, explanationQuestion.getExplanationEnglish()),
                new FieldUpdateUtil<>(exp::getExplanationVietnamese, exp::setExplanationVietnamese, explanationQuestion.getExplanationVietnamese()),
                new FieldUpdateUtil<>(exp::getIsActive, exp::setIsActive, explanationQuestion.getIsActive()),
                new FieldUpdateUtil<>(exp::getIsDelete, exp::setIsDelete, explanationQuestion.getIsDelete())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        exp.setUpdatedBy(user);
        ExplanationQuestion saved = explanationQuestionRepository.save(exp);

        return convertUtil.convertExplanationQuestionToDto(saved);
    }

    @Override
    public void deleteExplanationQuestion(HttpServletRequest httpServletRequest, Long id) {

    }
}
