package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchRangeTopicAndScoreScaleDto;
import com.doan2025.webtoeic.dto.request.ScoreScaleRequest;
import com.doan2025.webtoeic.dto.response.ScoreScaleResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScoreScaleService {

    ScoreScaleResponse getScoreScale(HttpServletRequest request, Long id);

    Page<ScoreScaleResponse> getScoreScales(HttpServletRequest request, SearchRangeTopicAndScoreScaleDto dto, Pageable pageable);

    ScoreScaleResponse createScoreScale(HttpServletRequest request, ScoreScaleRequest dto);

    ScoreScaleResponse updateScoreScale(ScoreScaleRequest dto, HttpServletRequest httpServletRequest);


}
