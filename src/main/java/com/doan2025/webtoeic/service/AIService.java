package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.response.AiResponse;

public interface AIService {
    String checkCallAI();

    AiResponse analysisWithAI(String url);
}
