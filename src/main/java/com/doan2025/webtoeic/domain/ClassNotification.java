package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.constants.enums.EClassNotificationType;
import com.doan2025.webtoeic.utils.TimeUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entity đại diện cho bảng thông báo lớp học (class_notification) trong hệ thống.
 * Lưu trữ thông tin về các thông báo liên quan đến lớp học, bao gồm mô tả, loại thông báo,
 * trạng thái hoạt động, trạng thái xóa, thời gian tạo/cập nhật, và thông tin người tạo/cập nhật.
 */
@Entity
@Data
@Table(name = "class_notification")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassNotification {
    /**
     * ID duy nhất của thông báo lớp học.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mô tả chi tiết của thông báo.
     */
    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    /**
     * Loại thông báo (NOTIFICATION(1), EXERCISE(2)).
     */
    @Column(name = "type_notification")
    @Enumerated(EnumType.STRING)
    private EClassNotificationType typeNotification;
    /**
     * Trạng thái hoạt động của thông báo co ghim lai hay khong (true: co, false: khong).
     */
    @Column(name = "is_pin")
    private Boolean isPin;

    // thoi gian nop bai tu
    @Column(name = "from_date")
    private Date fromDate;

    // han nop bai den
    @Column(name = "to_date")
    private Date toDate;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private Class clazz;
    /**
     * Trạng thái hoạt động của thông báo (true: hoạt động, false: không hoạt động).
     */
    @Column(name = "is_active")
    private Boolean isActive;

    /**
     * Trạng thái xóa của thông báo (true: đã xóa, false: chưa xóa).
     */
    @Column(name = "is_delete")
    private Boolean isDelete;

    /**
     * Thời gian tạo thông báo.
     */
    @Column(name = "created_at")
    private Date createdAt;

    /**
     * Thời gian cập nhật thông báo.
     */
    @Column(name = "updated_at")
    private Date updatedAt;

    /**
     * Người dùng tạo thông báo, liên kết với bảng User.
     */
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * Người dùng cập nhật thông báo, liên kết với bảng User.
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
        this.isPin = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = TimeUtil.getCurrentTimestamp();
    }

}
