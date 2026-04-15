package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.*;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.domain.ClassMember;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchClassDto;
import com.doan2025.webtoeic.dto.request.ClassRequest;
import com.doan2025.webtoeic.dto.response.ClassResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.ClassMemberRepository;
import com.doan2025.webtoeic.repository.ClassRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.ClassService;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;
    private final ClassMemberRepository classMemberRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;


    @Override
    public ClassResponse get(HttpServletRequest httpServletRequest, Long classId) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CLASS));
        if (!classMemberRepository.existsMemberInClass(classId, user.getId())
                && Objects.equals(user.getRole(), ERole.STUDENT)) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }

        return convertUtil.convertClassToDto(httpServletRequest, clazz);
    }

    @Override
    public Page<ClassResponse> getClasses(HttpServletRequest httpServletRequest, SearchClassDto dto, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Page<Class> classes = Page.empty();
        if (Objects.equals(user.getRole(), ERole.STUDENT) || Objects.equals(user.getRole(), ERole.TEACHER)) {
            List<Long> ids = classMemberRepository.findClassOfMember(user.getEmail());
            classes = classRepository.filterClass(dto, ids, pageable);
        } else if (Objects.equals(user.getRole(), ERole.CONSULTANT) || Objects.equals(user.getRole(), ERole.MANAGER)) {
            classes = classRepository.filterClass(dto, null, pageable);
        }
        return classes.map(item -> convertUtil.convertClassToDto(httpServletRequest, item));
    }

    @Override
    public void deleteClass(List<Long> ids, HttpServletRequest httpServletRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        for (Long id : ids) {
            Class clazz = classRepository.findById(id)
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CLASS));
            if (clazz.getTeacher().getCode().equals(user.getCode()) || ERole.CONSULTANT.getCode().equals(user.getRole().getCode())) {
                clazz.setStatus(EClassStatus.CANCELLED);
                classRepository.save(clazz);
                continue;
            }
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
    }

    @Override
    public ClassResponse updateClass(ClassRequest classRequest, HttpServletRequest httpServletRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Class clazz = classRepository.findById(classRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CLASS));
        List.of(
                new FieldUpdateUtil<>(clazz::getName, clazz::setName, classRequest.getName()),
                new FieldUpdateUtil<>(clazz::getDescription, clazz::setDescription, classRequest.getDescription()),
                new FieldUpdateUtil<>(clazz::getSubject, clazz::setSubject, classRequest.getSubject()),
                new FieldUpdateUtil<>(clazz::getTitle, clazz::setTitle, classRequest.getTitle()),
                new FieldUpdateUtil<>(clazz::getTeacher, clazz::setTeacher,
                        userRepository.findById(classRequest.getTeacher())
                                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER))),
                new FieldUpdateUtil<>(clazz::getStatus, clazz::setStatus, (classRequest.getStatus() == null) ? clazz.getStatus() : EClassStatus.fromValue(classRequest.getStatus()))
        ).forEach(FieldUpdateUtil::updateIfNeeded);
        clazz.setUpdatedBy(user);
        return convertUtil.convertClassToDto(httpServletRequest, classRepository.save(clazz));
    }

    @Override
    public ClassResponse createClass(HttpServletRequest request, ClassRequest classRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        User teacher = userRepository.findById(classRequest.getTeacher())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Class createClass = Class.builder()
                .description(classRequest.getDescription())
                .title(classRequest.getTitle())
                .name(classRequest.getName())
                .subject(classRequest.getSubject())
                .createdBy(user)
                .status(EClassStatus.PLANNING)
                .teacher(teacher)
                .build();
        Class clazz = classRepository.save(createClass);
        ClassMember classMember = ClassMember.builder()
                .member(teacher)
                .roleInClass(ERole.TEACHER)
                .clazz(clazz)
                .status(EJoinStatus.ACTIVE)
                .build();
        classMemberRepository.save(classMember);
        return convertUtil.convertClassToDto(request, clazz);
    }
}
