package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchRoomDto;
import com.doan2025.webtoeic.dto.request.RoomRequest;
import com.doan2025.webtoeic.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom(RoomRequest roomRequest);

    RoomResponse updateRoom(RoomRequest roomRequest);

    List<RoomResponse> getAllRooms(SearchRoomDto dto);
}
