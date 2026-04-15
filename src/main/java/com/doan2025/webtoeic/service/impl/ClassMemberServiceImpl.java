package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.*;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.domain.ClassMember;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchMemberInClassDto;
import com.doan2025.webtoeic.dto.request.ClassRequest;
import com.doan2025.webtoeic.dto.response.ClassMemberResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.ClassMemberRepository;
import com.doan2025.webtoeic.repository.ClassRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.ClassMemberService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.doan2025.webtoeic.utils.NotiUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class ClassMemberServiceImpl implements ClassMemberService {
    private final ClassRepository classRepository;
    private final ClassMemberRepository classMemberRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;
    private final NotiUtils notiUtils;


    @Override
    public Page<ClassMemberResponse> getMemberInClass(HttpServletRequest httpServletRequest, SearchMemberInClassDto request, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        if (Objects.isNull(request.getStatus()) || request.getStatus().isEmpty()) {
            request.setStatus(null);
        }
        if (Objects.equals(user.getRole(), ERole.STUDENT)) {
            return classMemberRepository.findMembersInClass(request, user.getEmail(), pageable)
                    .map(item -> convertUtil.convertClassMemberToDto(httpServletRequest, item));
        }
        return classMemberRepository.findMembersInClass(request, null, pageable)
                .map(item -> convertUtil.convertClassMemberToDto(httpServletRequest, item));
    }

    @Override
    public void addUserToClass(HttpServletRequest request, ClassRequest classRequest) {
        Class clazz = classRepository.findById(classRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CLASS));
        for (Long id : classRequest.getMemberIds()) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
            ClassMember classMember = classMemberRepository.findByClassAndMember(id, classRequest.getId());
            if (Objects.nonNull(classMember)) {
                if (classMember.getStatus().equals(EJoinStatus.DROPPED)) {
                    classMember.setStatus(EJoinStatus.ACTIVE);
                    classMemberRepository.save(classMember);
                }
                continue;
            } else {
                classMemberRepository.save(
                        ClassMember.builder()
                                .clazz(clazz)
                                .member(user)
                                .joinDate(new Date())
                                .roleInClass(user.getRole())
                                .status(EJoinStatus.ACTIVE)
                                .build());
            }
            notiUtils.sendNoti(List.of(user),
                    ENotiType.ADD_TO_CLASS,
                    Constants.ADD_TO_CLASS_CONTENT,
                    Constants.ADD_TO_CLASS_CONTENT,
                    clazz.getId());
        }
    }

    @Override
    public void removeUserFromClass(HttpServletRequest request, ClassRequest classRequest) {
        List<ClassMember> classMemberList = classMemberRepository.findByClassAndUser(classRequest);
        if (classMemberList.isEmpty()) {
            throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER);
        }
        for (ClassMember item : classMemberList) {
            item.setStatus(EJoinStatus.DROPPED);
            classMemberRepository.save(item);
        }
    }
}
