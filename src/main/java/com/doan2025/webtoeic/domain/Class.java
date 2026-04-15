package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.constants.enums.EClassStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entity đại diện cho bảng lớp học (class) trong hệ thống.
 * Lưu trữ thông tin về lớp học, bao gồm tên, mô tả, môn học, trạng thái,
 * số buổi học, thông tin giáo viên, và thông tin người tạo/cập nhật lớp.
 */

@Entity
@Data
@Table(name = "class")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Class {
    /**
     * ID duy nhất của lớp học.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên của lớp học.
     */
    @Lob
    @Column(name = "name", columnDefinition = "LONGTEXT")
    private String name;

    /**
     * Tổng số buổi học của lớp.
     */
    @Column(name = "total_session")
    private int totalSession;

    /**
     * Mô tả chi tiết về lớp học.
     */
    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    /**
     * Môn học của lớp.
     */
    @Lob
    @Column(name = "subject", columnDefinition = "LONGTEXT")
    private String subject;

    /**
     * Tiêu đề của lớp học.
     */
    @Lob
    @Column(name = "title", columnDefinition = "LONGTEXT")
    private String title;

    /**
     * Trạng thái của lớp học (ví dụ: ACTIVE, INACTIVE).
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EClassStatus status;

    /**
     * Thời gian tạo lớp học.
     */
    @Column(name = "created_at")
    private Date createdAt;

    /**
     * Thời gian cập nhật lớp học.
     */
    @Column(name = "updated_at")
    private Date updatedAt;

    /**
     * Giáo viên phụ trách lớp học, liên kết với bảng User.
     */
    @ManyToOne
    @JoinColumn(name = "teacher")
    private User teacher;

    /**
     * Người dùng tạo lớp học, liên kết với bảng User.
     */
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * Người dùng cập nhật lớp học, liên kết với bảng User.
     */
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
