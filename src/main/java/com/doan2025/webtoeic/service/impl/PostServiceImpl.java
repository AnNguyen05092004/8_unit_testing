package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ECategoryPost;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Post;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.PostRequest;
import com.doan2025.webtoeic.dto.response.PostResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.PostRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.PostService;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;


    @Override
    public Page<PostResponse> getAllPosts(SearchBaseDto dto, Pageable pageable) {
        if (dto.getCategoryPost() == null || dto.getCategoryPost().isEmpty()) {
            dto.setCategoryPost(null);
        }
        return postRepository.findPostByManagerWithFilter(dto, pageable);
    }

    @Override
    public Page<PostResponse> getOwnPosts(HttpServletRequest request, SearchBaseDto dto, Pageable pageable) {
        String email = jwtUtil.getEmailFromToken(request);
        dto.setEmail(email);
        if (dto.getCategoryPost() == null || dto.getCategoryPost().isEmpty()) {
            dto.setCategoryPost(null);
        }
        return postRepository.findOwnPostsWithFilter(dto, pageable);
    }

    @Override
    public Page<PostResponse> getPosts(SearchBaseDto dto, Pageable pageable) {
        if (dto.getCategoryPost() == null || dto.getCategoryPost().isEmpty()) {
            dto.setCategoryPost(null);
        }
        return postRepository.findPostFilter(dto, pageable);
    }

    @Override
    public PostResponse getPostDetail(HttpServletRequest request, Long id) {
        String email = "";
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            email = jwtUtil.getEmailFromToken(request);
        }
        return postRepository.findPostDetail(id, email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.POST));
    }


    @Override
    public PostResponse createPost(HttpServletRequest request, PostRequest postRequest) {
        Map<Supplier<Object>, ResponseObject> validations = Map.of(
                postRequest::getCategoryId, ResponseObject.CATEGORY,
                postRequest::getTitle, ResponseObject.TITLE,
                postRequest::getContent, ResponseObject.CONTENT
        );
        for (var entry : validations.entrySet()) {
            if (entry.getKey().get() == null) {
                throw new WebToeicException(ResponseCode.IS_NULL, entry.getValue());
            }
        }
        Post post = modelMapper.map(postRequest, Post.class);
        String email = jwtUtil.getEmailFromToken(request);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        post.setAuthor(user);
        ECategoryPost categoryPost;
        try {
            categoryPost = ECategoryPost.fromValue(postRequest.getCategoryId());
        } catch (IllegalArgumentException e) {
            throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CATEGORY);
        }
        post.setCategoryPost(categoryPost);
        return modelMapper.map(postRepository.save(post), PostResponse.class);
    }

    @Override
    public PostResponse updatePost(HttpServletRequest request, PostRequest postRequest) {
        String email = jwtUtil.getEmailFromToken(request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Post post = postRepository.findById(postRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.POST));
        if (user.getRole().equals(ERole.CONSULTANT) && post.getAuthor().getEmail().equals(email)) {
            List.of(
                    new FieldUpdateUtil<>(post::getTitle, post::setTitle, postRequest.getTitle()),
                    new FieldUpdateUtil<>(post::getContent, post::setContent, postRequest.getContent()),
                    new FieldUpdateUtil<>(post::getThemeUrl, post::setThemeUrl, postRequest.getThemeUrl()),
                    new FieldUpdateUtil<>(post::getCategoryPost, post::setCategoryPost, ECategoryPost.fromValue(postRequest.getCategoryId()))
            ).forEach(FieldUpdateUtil::updateIfNeeded);
            return modelMapper.map(postRepository.save(post), PostResponse.class);
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
    }

    @Override
    public PostResponse disableOrDeletePost(HttpServletRequest request, PostRequest postRequest) {
        String email = jwtUtil.getEmailFromToken(request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Post post = postRepository.findById(postRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.POST));
//        if(user.getRole().equals(ERole.MANAGER)){
        // function: disable post
        if (postRequest.getIsActive() != null && !postRequest.getIsActive().equals(post.getIsActive())) {
            post.setIsActive(postRequest.getIsActive());
        }
        // function: delete post
        if (postRequest.getIsDelete() != null && !post.getIsDelete().equals(postRequest.getIsDelete())) {
            post.setIsDelete(postRequest.getIsDelete());
        }
        return modelMapper.map(postRepository.save(post), PostResponse.class);
//        }
//        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
    }
}
