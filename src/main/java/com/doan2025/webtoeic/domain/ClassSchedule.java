package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.constants.enums.EScheduleStatus;
import com.doan2025.webtoeic.utils.TimeUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entity đại diện cho bảng lịch học (class_schedule) trong hệ thống.
 * Lưu trữ thông tin về lịch học của một lớp, bao gồm tiêu đề, thời gian bắt đầu và kết thúc,
 * trạng thái hoạt động, trạng thái xóa, và thông tin người tạo/cập nhật lịch học.
 */
@Entity
@Data
@Table(name = "class_schedule")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassSchedule {
    /**
     * ID duy nhất của lịch học.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Lớp học mà lịch học thuộc về, liên kết với bảng Class.
     */
    @ManyToOne
    @JoinColumn(name = "class")
    private Class clazz;

    @ManyToOne
    @JoinColumn(name = "room")
    private Room room;

    /**
     * Tiêu đề của lịch học.
     */
    @Lob
    @Column(name = "title", columnDefinition = "LONGTEXT")
    private String title;

    /**
     * Thời gian bắt đầu của lịch học.
     */
    @Column(name = "start_at")
    private Date startAt;

    /**
     * Thời gian kết thúc của lịch học.
     */
    @Column(name = "end_at")
    private Date endAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EScheduleStatus status;

    /**
     * Trạng thái hoạt động của lịch học (true: hoạt động, false: không hoạt động).
     */
    @Column(name = "is_active")
    private Boolean isActive;

    /**
     * Trạng thái xóa của lịch học (true: đã xóa, false: chưa xóa).
     */
    @Column(name = "is_delete")
    private Boolean isDelete;

    @Column(name = "is_attendance")
    private Boolean isAttendance;

    /**
     * Thời gian tạo lịch học.
     */
    @Column(name = "created_at")
    private Date createdAt;

    /**
     * Thời gian cập nhật lịch học.
     */
    @Column(name = "updated_at")
    private Date updatedAt;

    /**
     * Người dùng tạo lịch học, liên kết với bảng User.
     */
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * Người dùng cập nhật lịch học, liên kết với bảng User.
     */
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @PrePersist
    protected void onCreate() {
        this.isActive = true;
        this.isDelete = false;
        this.createdAt = TimeUtil.getCurrentTimestamp();
        this.updatedAt = null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = TimeUtil.getCurrentTimestamp();
    }
}
