package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.PostRequest;
import com.doan2025.webtoeic.dto.response.PostResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    Page<PostResponse> getAllPosts(SearchBaseDto dto, Pageable pageable);
    Page<PostResponse> getOwnPosts(HttpServletRequest request, SearchBaseDto dto, Pageable pageable);
    Page<PostResponse> getPosts(SearchBaseDto dto, Pageable pageable);
    PostResponse getPostDetail(HttpServletRequest request, Long id); // get detail
    PostResponse createPost(HttpServletRequest request, PostRequest postRequest);
    PostResponse updatePost(HttpServletRequest request, PostRequest postRequest);
    PostResponse disableOrDeletePost(HttpServletRequest request, PostRequest postRequest);
}
