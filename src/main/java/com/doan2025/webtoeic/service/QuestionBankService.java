package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchBankDto;
import com.doan2025.webtoeic.dto.request.BankRequest;
import com.doan2025.webtoeic.dto.response.BankResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionBankService {
    BankResponse getQuestionBank(HttpServletRequest httpServletRequest, Long id);

    Page<BankResponse> getQuestionBanks(HttpServletRequest httpServletRequest, SearchBankDto dto, Pageable pageable);

    BankResponse saveQuestionBank(HttpServletRequest httpServletRequest, BankRequest bank);

    BankResponse updateQuestionBank(HttpServletRequest httpServletRequest, BankRequest bank);
}
