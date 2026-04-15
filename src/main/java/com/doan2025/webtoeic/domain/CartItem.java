package com.doan2025.webtoeic.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entity đại diện cho bảng mục trong giỏ hàng (cart_item) trong hệ thống.
 * Lưu trữ thông tin về khóa học mà người dùng thêm vào giỏ hàng, bao gồm liên kết
 * đến người dùng, khóa học và thời gian tạo mục.
 */

@Entity
@Table(name = "cart_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    /**
     * ID duy nhất của mục trong giỏ hàng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Người dùng sở hữu mục trong giỏ hàng, liên kết với bảng User.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    /**
     * Khóa học được thêm vào giỏ hàng, liên kết với bảng Course.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course")
    private Course course;

    /**
     * Thời gian tạo mục trong giỏ hàng.
     */
    @Column(name = "created_at")
    private Date createdAt;

    /**
     * Thiết lập thời gian tạo mặc định khi thêm mới mục vào giỏ hàng.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}
