package com.doan2025.webtoeic.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class CartItemResponse {
    private Long id;
    private CourseResponse course;
    private Date createdAt;
}
