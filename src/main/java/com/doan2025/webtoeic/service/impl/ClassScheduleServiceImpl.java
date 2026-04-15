package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.EScheduleStatus;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.domain.ClassSchedule;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchScheduleSto;
import com.doan2025.webtoeic.dto.request.ClassScheduleRequest;
import com.doan2025.webtoeic.dto.response.ClassScheduleResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.service.ClassScheduleService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class ClassScheduleServiceImpl implements ClassScheduleService {
    private final ClassScheduleRepository classScheduleRepository;
    private final ClassRepository classRepository;
    private final ClassMemberRepository classMemberRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;
    private final RoomRepository roomRepository;


    @Override
    public Page<?> detailStatisticAttendance(HttpServletRequest httpServletRequest, Long scheduleId, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SCHEDULE));
        if ((Objects.equals(user.getRole(), ERole.TEACHER)
                && classMemberRepository.existsMemberInClass(schedule.getClazz().getId(), user.getId()))
                || Objects.equals(user.getRole(), ERole.CONSULTANT)
                || Objects.equals(user.getRole(), ERole.MANAGER)) {
            return attendanceRepository.detailStatisticAttendance(scheduleId, pageable);
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
    }

    @Override
    public Page<?> overviewStatisticAttendance(HttpServletRequest httpServletRequest, Long classId, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        if (Objects.equals(user.getRole(), ERole.TEACHER) && classMemberRepository.existsMemberInClass(classId, user.getId())
                || Objects.equals(user.getRole(), ERole.CONSULTANT)
                || Objects.equals(user.getRole(), ERole.MANAGER)) {
            return attendanceRepository.overviewStatisticAttendance(classId, pageable);
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
    }

    @Override
    public Page<?> overviewStudentAttendance(HttpServletRequest httpServletRequest, Long classId, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        if (Objects.equals(user.getRole(), ERole.TEACHER) && classMemberRepository.existsMemberInClass(classId, user.getId())
                || Objects.equals(user.getRole(), ERole.CONSULTANT)
                || Objects.equals(user.getRole(), ERole.MANAGER)) {
            return attendanceRepository.overviewStudentAttendance(classId, pageable);
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
    }

    @Override
    public ClassScheduleResponse getScheduleDetail(HttpServletRequest request, Long scheduleId) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SCHEDULE));
        if (classMemberRepository.existsMemberInClass(scheduleId, user.getId())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
        return convertUtil.convertScheduleToDto(request, schedule);
    }

    @Override
    public Page<?> getClassSchedule(HttpServletRequest request, SearchScheduleSto dto, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Page<ClassSchedule> result;
        if (Objects.isNull(dto.getClassId()) || dto.getClassId().isEmpty()) {
            dto.setClassId(null);
        }
        if (Objects.isNull(dto.getTeacherId()) || dto.getTeacherId().isEmpty()) {
            dto.setTeacherId(null);
        }
        if (Objects.isNull(dto.getStatus()) || dto.getStatus().isEmpty()) {
            dto.setStatus(null);
        }
        Pageable customPageable = PageRequest.of(
                pageable.getPageNumber(),
                100000,
                pageable.getSort()
        );
        if (Objects.equals(user.getRole(), ERole.MANAGER) || Objects.equals(user.getRole(), ERole.CONSULTANT)) {
            result = classScheduleRepository.filterSchedule(dto, null, customPageable);
        } else if (Objects.equals(user.getRole(), ERole.TEACHER) || Objects.equals(user.getRole(), ERole.STUDENT)) {
            result = classScheduleRepository.filterSchedule(dto, classMemberRepository.findClassOfMember(user.getEmail()), customPageable);
        } else {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
        return result.map(item -> convertUtil.convertScheduleToDto(request, item));
    }

    @Override
    public List<?> createScheduleInClass(HttpServletRequest request, List<ClassScheduleRequest> classScheduleRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        List<ClassSchedule> scheduleSaved = new ArrayList<>();
        for (ClassScheduleRequest item : classScheduleRequest) {
            Class clazz = classRepository.findById(item.getClassId())
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CLASS));
            List<Long> isUsed = classScheduleRepository
                    .existsScheduleByRoomIdAndStartAtAndEndAt(item.getStartAt(), item.getEndAt(), item.getRoomId());
            if (!isUsed.isEmpty()) {
                throw new WebToeicException(ResponseCode.NOT_AVAILABLE, ResponseObject.ROOM);
            }
            if (Objects.isNull(item.getRoomId())) {
                throw new WebToeicException(ResponseCode.IS_NULL, ResponseObject.ROOM);
            }
            List<Long> isHasSchedule = classScheduleRepository
                    .existsScheduleByClassIdAndStartAtAndEndAt(item.getStartAt(), item.getEndAt(), item.getClassId());
            if (!isHasSchedule.isEmpty()) {
                throw new WebToeicException(ResponseCode.NOT_AVAILABLE, ResponseObject.SCHEDULE);
            }
            ClassSchedule classSchedule = ClassSchedule.builder()
                    .title(item.getTitle())
                    .startAt(item.getStartAt())
                    .endAt(item.getEndAt())
                    .room(roomRepository.findById(item.getRoomId())
                            .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ROOM)))
                    .status(EScheduleStatus.ACTIVE)
                    .createdBy(user)
                    .clazz(clazz)
                    .build();
            scheduleSaved.add(classScheduleRepository.save(classSchedule));
        }
        return scheduleSaved.stream()
                .map(item -> convertUtil.convertScheduleToDto(request, item))
                .collect(Collectors.toList());
    }


    @Override
    public ClassScheduleResponse updateScheduleInClass(HttpServletRequest request, ClassScheduleRequest classScheduleRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        ClassSchedule schedule = classScheduleRepository.findById(classScheduleRequest.getClassScheduleId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SCHEDULE));
        if (!Objects.equals(user.getRole(), ERole.CONSULTANT)
                && !Objects.equals(user.getEmail(), schedule.getClazz().getTeacher().getEmail())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
        List.of(
                new FieldUpdateUtil<>(schedule::getTitle, schedule::setTitle, classScheduleRequest.getTitle()),
                new FieldUpdateUtil<>(schedule::getStartAt, schedule::setStartAt, classScheduleRequest.getStartAt()),
                new FieldUpdateUtil<>(schedule::getEndAt, schedule::setEndAt, classScheduleRequest.getEndAt()),
                new FieldUpdateUtil<>(schedule::getStatus, schedule::setStatus, EScheduleStatus.fromValue(classScheduleRequest.getStatus())),
                new FieldUpdateUtil<>(schedule::getIsActive, schedule::setIsActive, classScheduleRequest.getIsActive()),
                new FieldUpdateUtil<>(schedule::getIsDelete, schedule::setIsDelete, classScheduleRequest.getIsDelete())
        ).forEach(FieldUpdateUtil::updateIfNeeded);
        if (!Objects.equals(classScheduleRequest.getRoomId(), schedule.getRoom().getId())
                || !Objects.equals(classScheduleRequest.getStartAt(), schedule.getStartAt())
                || !Objects.equals(classScheduleRequest.getEndAt(), schedule.getEndAt())) {
            List<Long> isUsed = classScheduleRepository
                    .existsScheduleByRoomIdAndStartAtAndEndAt(
                            classScheduleRequest.getStartAt(),
                            classScheduleRequest.getEndAt(),
                            classScheduleRequest.getRoomId());
            if (!isUsed.isEmpty()) {
                throw new WebToeicException(ResponseCode.NOT_AVAILABLE, ResponseObject.ROOM);
            }
        }
        schedule.setUpdatedBy(user);
        return convertUtil.convertScheduleToDto(request, classScheduleRepository.save(schedule));
    }

    @Override
    public void cancelledScheduleInClass(HttpServletRequest request, List<Long> idsOfClassSchedule) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        for (Long id : idsOfClassSchedule) {
            ClassSchedule schedule = classScheduleRepository.findById(id)
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SCHEDULE));
            if (schedule.getClazz().getTeacher().getCode().equals(user.getCode()) || ERole.CONSULTANT.getCode().equals(user.getRole().getCode())) {
                schedule.setStatus(EScheduleStatus.CANCELLED);
                schedule.setIsActive(false);
                classScheduleRepository.save(schedule);
                continue;
            }
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }
    }
}
