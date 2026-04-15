package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.QuestionBank;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBankDto;
import com.doan2025.webtoeic.dto.request.BankRequest;
import com.doan2025.webtoeic.dto.response.BankResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.QuestionBankRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.QuestionBankService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {Exception.class, WebToeicException.class})
public class QuestionBankServiceImpl implements QuestionBankService {
    private final QuestionBankRepository questionBankRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;

    @Override
    public BankResponse getQuestionBank(HttpServletRequest httpServletRequest, Long id) {
        return convertUtil.convertQuestionBankToDto(questionBankRepository.findById(id)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.BANK)));
    }

    @Override
    public Page<BankResponse> getQuestionBanks(HttpServletRequest httpServletRequest, SearchBankDto dto, Pageable pageable) {
        Page<QuestionBank> questionBanks = questionBankRepository.filter(dto, pageable);
        return questionBanks.map(convertUtil::convertQuestionBankToDto);
    }

    @Override
    public BankResponse saveQuestionBank(HttpServletRequest httpServletRequest, BankRequest bank) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        QuestionBank questionBank = QuestionBank.builder()
                .createBy(user)
                .linkUrl(bank.getUrl())
                .title(bank.getQuestionBankTitle())
                .build();
        QuestionBank saved = questionBankRepository.save(questionBank);
        return convertUtil.convertQuestionBankToDto(saved);
    }

    @Override
    public BankResponse updateQuestionBank(HttpServletRequest httpServletRequest, BankRequest bank) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        QuestionBank questionBank = questionBankRepository.findById(bank.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.BANK));

        List.of(
                new FieldUpdateUtil<>(questionBank::getTitle, questionBank::setTitle, bank.getQuestionBankTitle()),
                new FieldUpdateUtil<>(questionBank::getLinkUrl, questionBank::setLinkUrl, bank.getUrl()),
                new FieldUpdateUtil<>(questionBank::getIsActive, questionBank::setIsActive, bank.getIsActive()),
                new FieldUpdateUtil<>(questionBank::getIsDelete, questionBank::setIsDelete, bank.getIsDeleted())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        questionBank.setUpdateBy(user);
        QuestionBank saved = questionBankRepository.save(questionBank);
        return convertUtil.convertQuestionBankToDto(saved);
    }
}
