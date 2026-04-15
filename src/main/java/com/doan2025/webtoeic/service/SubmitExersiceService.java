package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchSubmitExerciseDto;
import com.doan2025.webtoeic.dto.request.SubmitExerciseRequest;
import com.doan2025.webtoeic.dto.response.SubmitExerciseResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubmitExersiceService {
    SubmitExerciseResponse getDetailSubmitExercise(HttpServletRequest httpServletRequest, Long submitId);

    Page<SubmitExerciseResponse> getListSubmitExercise(HttpServletRequest httpServletRequest, SearchSubmitExerciseDto dto, Pageable pageable);

    SubmitExerciseResponse createSubmitExercise(HttpServletRequest httpServletRequest, SubmitExerciseRequest request);

    SubmitExerciseResponse updateSubmitExercise(HttpServletRequest httpServletRequest, SubmitExerciseRequest request);

    SubmitExerciseResponse deleteOrCancelSubmitExercise(HttpServletRequest httpServletRequest, SubmitExerciseRequest request);
}
