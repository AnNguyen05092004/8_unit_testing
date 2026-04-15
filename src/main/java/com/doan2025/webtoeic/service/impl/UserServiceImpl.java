package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.UserRequest;
import com.doan2025.webtoeic.dto.response.*;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.UserService;
import com.doan2025.webtoeic.utils.CommonUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Page<UserResponse> getListUserFilter(HttpServletRequest request, SearchBaseDto dto, Pageable pageable) {
        String email = jwtUtil.getEmailFromToken(request);
        if (email == null) {
            throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.EMAIL);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        List<ERole> roles = new ArrayList<>();

        if (user.getRole().equals(ERole.MANAGER)) {
            roles.addAll(Constants.ROLE_BELOW_MANAGER);
        } else {
            roles.addAll(Constants.ROLE_BELOW_CONSULTANT);
        }
        if (dto.getUserRoles() == null || dto.getUserRoles().isEmpty()) {
            dto.setUserRoles(null);
        }
        return userRepository.findListUserFilter(dto, roles, pageable);
    }

    @Override
    public UserResponse getUserCurrent(HttpServletRequest request) {
        String email = jwtUtil.getEmailFromToken(request);
        if (email == null) {
            throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.EMAIL);
        }
        return userRepository.findUser(email).orElseThrow(
                () -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER)
        );
    }

    @Override
    public UserResponse getUserDetails(UserRequest request) {
        if (request.getId() == null) {
            throw new WebToeicException(ResponseCode.IS_NULL, ResponseObject.ID);
        }
        return userRepository.findUserById(request).orElseThrow(
                () -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER)
        );
    }

    @Override
    public UserResponse updateUserDetails(HttpServletRequest httpServletRequest, UserRequest request) {

        String email = jwtUtil.getEmailFromToken(httpServletRequest);
        if (email == null) {
            throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        // function: change password
        if (request.getPassword() != null && request.getOldPassword() != null) {
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new WebToeicException(ResponseCode.NOT_MATCHED, ResponseObject.PASSWORD);
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            return modelMapper.map(userRepository.save(user), UserResponse.class);
        }

        // function: update info
        List.of(
                new FieldUpdateUtil<>(user::getFirstName, user::setFirstName, request.getFirstName()),
                new FieldUpdateUtil<>(user::getLastName, user::setLastName, request.getLastName()),
                new FieldUpdateUtil<>(user::getPhone, user::setPhone, request.getPhone()),
                new FieldUpdateUtil<>(user::getAddress, user::setAddress, request.getAddress()),
                new FieldUpdateUtil<>(user::getDob, user::setDob, CommonUtil.parseDate(request.getDob())),
                new FieldUpdateUtil<>(user::getGender, user::setGender, CommonUtil.convertIntegerToEGender(request.getGender())),
                new FieldUpdateUtil<>(user::getAvatarUrl, user::setAvatarUrl, request.getAvatarUrl())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        if (user.getRole().equals(ERole.MANAGER)) {

        } else if (user.getRole().equals(ERole.CONSULTANT)) {

        } else if (user.getRole().equals(ERole.TEACHER)) {

        } else {
            List.of(
                    new FieldUpdateUtil<>(user.getStudent()::getEducation, user.getStudent()::setEducation, request.getEducation()),
                    new FieldUpdateUtil<>(user.getStudent()::getMajor, user.getStudent()::setMajor, request.getMajor())
            ).forEach(FieldUpdateUtil::updateIfNeeded);
        }

        User savedUser = userRepository.save(user);
        UserResponse savedUserResponse = modelMapper.map(savedUser, UserResponse.class);

        if (user.getRole().equals(ERole.MANAGER)) {
            ManagerResponse managerResponse = modelMapper.map(savedUser.getManager(), ManagerResponse.class);
            savedUserResponse.setManager(managerResponse);
        } else if (user.getRole().equals(ERole.CONSULTANT)) {
            ConsultantResponse savedConsultant = modelMapper.map(savedUser.getConsultant(), ConsultantResponse.class);
            savedUserResponse.setConsultant(savedConsultant);
        } else if (user.getRole().equals(ERole.TEACHER)) {
            TeacherResponse savedTeacherResponse = modelMapper.map(savedUser.getTeacher(), TeacherResponse.class);
            savedUserResponse.setTeacher(savedTeacherResponse);
        } else {
            StudentResponse studentResponse = modelMapper.map(savedUser.getStudent(), StudentResponse.class);
            savedUserResponse.setStudent(studentResponse);
        }

        return savedUserResponse;
    }

    // function: delete or disable user
    @Override
    public UserResponse deleteOrDisableUser(UserRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(
                        () -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER)
                );
        // function: disable user
        if (request.getIsActive() != null && !request.getIsActive().equals(user.getIsActive())) {
            user.setIsActive(request.getIsActive());
        }
        // function: delete user
        if (request.getIsDelete() != null && !request.getIsDelete().equals(user.getIsDelete())) {
            user.setIsDelete(request.getIsDelete());
        }
        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    // function: reset password
    @Override
    public void resetPassword(UserRequest request) {
        // check token not null
        if (request.getToken() == null) {
            throw new WebToeicException(ResponseCode.IS_NULL, ResponseObject.TOKEN);
        }
        // check mail not null
        String email = jwtUtil.getEmailFromTokenString(request.getToken());
        if (email == null) {
            throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.EMAIL);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        if (!user.getIsActive() && user.getIsDelete()) {
            throw new WebToeicException(ResponseCode.NOT_AVAILABLE, ResponseObject.USER);
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

}
