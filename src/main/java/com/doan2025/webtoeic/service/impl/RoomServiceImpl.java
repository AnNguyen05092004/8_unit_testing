package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Room;
import com.doan2025.webtoeic.dto.SearchRoomDto;
import com.doan2025.webtoeic.dto.request.RoomRequest;
import com.doan2025.webtoeic.dto.response.RoomResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.RoomRepository;
import com.doan2025.webtoeic.service.RoomService;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    @Override
    public RoomResponse createRoom(RoomRequest roomRequest) {
        return modelMapper.map(
                roomRepository.save(
                        Room.builder()
                                .name(roomRequest.getName())
                                .description(roomRequest.getDescription())
                                .build()), RoomResponse.class);
    }

    @Override
    public RoomResponse updateRoom(RoomRequest roomRequest) {
        Room room = roomRepository.findById(roomRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ROOM));
        List.of(
                new FieldUpdateUtil<>(room::getName, room::setName, roomRequest.getName()),
                new FieldUpdateUtil<>(room::getDescription, room::setDescription, roomRequest.getDescription()),
                new FieldUpdateUtil<>(room::getIsDelete, room::setIsDelete, roomRequest.getIsDelete()),
                new FieldUpdateUtil<>(room::getIsActive, room::setIsActive, roomRequest.getIsActive())
        ).forEach(FieldUpdateUtil::updateIfNeeded);
        return modelMapper.map(roomRepository.save(room), RoomResponse.class);
    }

    @Override
    public List<RoomResponse> getAllRooms(SearchRoomDto dto) {
        return roomRepository.filter(dto).stream()
                .map(room -> modelMapper.map(room, RoomResponse.class))
                .collect(Collectors.toList());
    }
}
