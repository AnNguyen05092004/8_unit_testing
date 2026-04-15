package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchRangeTopicAndScoreScaleDto;
import com.doan2025.webtoeic.dto.request.RangeTopicRequest;
import com.doan2025.webtoeic.dto.response.RangeTopicResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RangeTopicService {
    RangeTopicResponse getRangeTopic(HttpServletRequest request, Long id);

    Page<RangeTopicResponse> getRangeTopics(HttpServletRequest request, SearchRangeTopicAndScoreScaleDto dto, Pageable pageable);

    RangeTopicResponse createRangeTopic(HttpServletRequest request, RangeTopicRequest rangeTopicRequest);

    RangeTopicResponse updateRangeTopic(RangeTopicRequest rangeTopicRequest, HttpServletRequest httpServletRequest);

}
