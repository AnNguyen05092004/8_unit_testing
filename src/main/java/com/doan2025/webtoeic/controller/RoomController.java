package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.SearchRoomDto;
import com.doan2025.webtoeic.dto.request.RoomRequest;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/room")
public class RoomController {
    private final RoomService roomService;

    @PostMapping("/filter")
    @PreAuthorize("hasRole('TEACHER') or hasRole('MANAGER') or  hasRole('CONSULTANT')")
    public ApiResponse<?> filterAll(HttpServletRequest request, @RequestBody SearchRoomDto dto) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.ROOM, roomService.getAllRooms(dto));
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('TEACHER') or hasRole('MANAGER') or  hasRole('CONSULTANT')")
    public ApiResponse<?> update(HttpServletRequest request, @RequestBody RoomRequest roomRequest) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.ROOM, roomService.updateRoom(roomRequest));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('TEACHER') or hasRole('MANAGER') or  hasRole('CONSULTANT')")
    public ApiResponse<?> create(HttpServletRequest request, @RequestBody RoomRequest roomRequest) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.ROOM, roomService.createRoom(roomRequest));
    }

}
