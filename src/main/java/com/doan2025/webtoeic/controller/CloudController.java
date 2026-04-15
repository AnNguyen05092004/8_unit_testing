package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.request.FileRequest;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.CloudService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cloud")
@RequiredArgsConstructor
public class CloudController {
    private final CloudService cloudService;

    @PostMapping(value = "/upload")
    public ApiResponse<String> upload(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.of(ResponseCode.UPLOAD_SUCCESS, ResponseObject.FILE, cloudService.uploadFile(file));
    }

    @PostMapping(value = "/delete")
    public ApiResponse<Map> delete(HttpServletRequest request, @RequestBody FileRequest dto) throws IOException {
        return ApiResponse.of(ResponseCode.DELETE_SUCCESS, ResponseObject.FILE, cloudService.deleteFile(dto));
    }
}
