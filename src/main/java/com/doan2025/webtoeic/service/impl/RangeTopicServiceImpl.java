package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.RangeTopic;
import com.doan2025.webtoeic.dto.SearchRangeTopicAndScoreScaleDto;
import com.doan2025.webtoeic.dto.request.RangeTopicRequest;
import com.doan2025.webtoeic.dto.response.RangeTopicResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.RangeTopicRepository;
import com.doan2025.webtoeic.service.RangeTopicService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class RangeTopicServiceImpl implements RangeTopicService {
    private final RangeTopicRepository rangeTopicRepository;
    private final ConvertUtil convertUtil;

    @Override
    public RangeTopicResponse getRangeTopic(HttpServletRequest request, Long id) {
        RangeTopic topic = rangeTopicRepository.findById(id)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.RANGE_TOPIC));

        return convertUtil.convertRangeTopicToDto(request, topic);
    }

    @Override
    public Page<RangeTopicResponse> getRangeTopics(HttpServletRequest request, SearchRangeTopicAndScoreScaleDto dto, Pageable pageable) {
        Page<RangeTopic> pages = rangeTopicRepository.filter(dto, pageable);

        return pages.map(item -> convertUtil.convertRangeTopicToDto(request, item));
    }

    @Override
    public RangeTopicResponse createRangeTopic(HttpServletRequest request, RangeTopicRequest rangeTopicRequest) {

        RangeTopic rangeTopic = RangeTopic.builder()
                .content(rangeTopicRequest.getContent())
                .description(rangeTopicRequest.getDescription())
                .vietnamese(rangeTopicRequest.getVietnamese())
                .build();
        
        rangeTopic = rangeTopicRepository.save(rangeTopic);

        return convertUtil.convertRangeTopicToDto(request, rangeTopic);
    }

    @Override
    public RangeTopicResponse updateRangeTopic(RangeTopicRequest rangeTopicRequest, HttpServletRequest httpServletRequest) {

        RangeTopic topic = rangeTopicRepository.findById(rangeTopicRequest.getRangeTopicId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.RANGE_TOPIC));

        List.of(
                new FieldUpdateUtil<>(topic::getContent, topic::setContent, rangeTopicRequest.getContent()),
                new FieldUpdateUtil<>(topic::getDescription, topic::setDescription, rangeTopicRequest.getContent()),
                new FieldUpdateUtil<>(topic::getVietnamese, topic::setVietnamese, rangeTopicRequest.getVietnamese()),
                new FieldUpdateUtil<>(topic::getIsDelete, topic::setIsDelete, rangeTopicRequest.getIsDelete()),
                new FieldUpdateUtil<>(topic::getIsActive, topic::setIsActive, rangeTopicRequest.getIsActive())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        return convertUtil.convertRangeTopicToDto(httpServletRequest, rangeTopicRepository.save(topic));
    }

}
