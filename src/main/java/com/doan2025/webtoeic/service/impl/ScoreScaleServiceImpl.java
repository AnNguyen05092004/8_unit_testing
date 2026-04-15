package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.ScoreScale;
import com.doan2025.webtoeic.dto.SearchRangeTopicAndScoreScaleDto;
import com.doan2025.webtoeic.dto.request.ScoreScaleRequest;
import com.doan2025.webtoeic.dto.response.ScoreScaleResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.ScoreScaleRepository;
import com.doan2025.webtoeic.service.ScoreScaleService;
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
public class ScoreScaleServiceImpl implements ScoreScaleService {

    private final ScoreScaleRepository scoreScaleRepository;
    private final ConvertUtil convertUtil;

    @Override
    public ScoreScaleResponse getScoreScale(HttpServletRequest request, Long id) {
        return convertUtil.convertScoreScaleToDto(request, scoreScaleRepository.findById(id)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SCORE_SCALE)
                )
        );
    }

    @Override
    public Page<ScoreScaleResponse> getScoreScales(HttpServletRequest request, SearchRangeTopicAndScoreScaleDto dto, Pageable pageable) {
        Page<ScoreScale> pages = scoreScaleRepository.filter(dto, pageable);
        return pages.map(scoreScale -> convertUtil.convertScoreScaleToDto(request, scoreScale));
    }

    @Override
    public ScoreScaleResponse createScoreScale(HttpServletRequest request, ScoreScaleRequest dto) {
        ScoreScale scoreScale = ScoreScale.builder()
                .title(dto.getTitle())
                .fromScore(dto.getFromScore())
                .toScore(dto.getToScore())
                .build();
        return convertUtil.convertScoreScaleToDto(request, scoreScaleRepository.save(scoreScale));
    }

    @Override
    public ScoreScaleResponse updateScoreScale(ScoreScaleRequest dto, HttpServletRequest httpServletRequest) {
        ScoreScale scoreScale = scoreScaleRepository.findById(dto.getScoreScaleId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SCORE_SCALE));
        List.of(
                new FieldUpdateUtil<>(scoreScale::getTitle, scoreScale::setTitle, dto.getTitle()),
                new FieldUpdateUtil<>(scoreScale::getFromScore, scoreScale::setFromScore, dto.getFromScore()),
                new FieldUpdateUtil<>(scoreScale::getToScore, scoreScale::setToScore, dto.getToScore()),
                new FieldUpdateUtil<>(scoreScale::getIsActive, scoreScale::setIsActive, dto.getIsActive()),
                new FieldUpdateUtil<>(scoreScale::getIsDelete, scoreScale::setIsDelete, dto.getIsDelete())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        return convertUtil.convertScoreScaleToDto(httpServletRequest, scoreScaleRepository.save(scoreScale));
    }
}
