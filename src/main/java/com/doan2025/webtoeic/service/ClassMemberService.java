package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchMemberInClassDto;
import com.doan2025.webtoeic.dto.request.ClassRequest;
import com.doan2025.webtoeic.dto.response.ClassMemberResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClassMemberService {

    Page<ClassMemberResponse> getMemberInClass(HttpServletRequest httpServletRequest, SearchMemberInClassDto request, Pageable pageable);

    void addUserToClass(HttpServletRequest request, ClassRequest classRequest);

    void removeUserFromClass(HttpServletRequest request, ClassRequest classRequest);
}
