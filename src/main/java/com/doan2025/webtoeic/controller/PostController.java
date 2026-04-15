package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.PostRequest;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.dto.response.PostResponse;
import com.doan2025.webtoeic.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostService postService;


    @PostMapping("/get-posts")
    public ApiResponse<Page<PostResponse>> getPosts(HttpServletRequest request,
                                                    @RequestBody SearchBaseDto dto,
                                                    Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.POST, postService.getPosts(dto, pageable));
    }

    @GetMapping()
    public ApiResponse<PostResponse> getPostDetail(HttpServletRequest request, @RequestParam("id") Long id) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.POST, postService.getPostDetail(request, id));
    }

    @PostMapping("/own-posts")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ApiResponse<Page<PostResponse>> getOwnPosts(HttpServletRequest request,
                                                       @RequestBody SearchBaseDto dto,
                                                       Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.POST, postService.getOwnPosts(request, dto, pageable));
    }

    @PostMapping("/all-posts")
    @PreAuthorize("hasRole('MANAGER') OR hasRole('CONSULTANT')")
    public ApiResponse<Page<PostResponse>> getAllPosts(HttpServletRequest request,
                                                       @RequestBody SearchBaseDto dto,
                                                       Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.POST, postService.getAllPosts(dto, pageable));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ApiResponse<PostResponse> createPost(HttpServletRequest request, @RequestBody PostRequest postRequest) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS, ResponseObject.POST, postService.createPost(request, postRequest));
    }

    @PostMapping("/update-info")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<PostResponse> updatePost(HttpServletRequest request, @RequestBody PostRequest postRequest) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS, ResponseObject.POST, postService.updatePost(request, postRequest));
    }

    @PostMapping("/update-status")
    @PreAuthorize("hasRole('MANAGER') OR hasRole('CONSULTANT')")
    public ApiResponse<PostResponse> disableOrDeletePost(HttpServletRequest request, @RequestBody PostRequest postRequest) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS, ResponseObject.POST, postService.disableOrDeletePost(request, postRequest));
    }


}
