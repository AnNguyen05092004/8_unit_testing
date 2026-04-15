package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.utils.TimeUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@Table(name = "submit_excercise_in_noti")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmitExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Đường dẫn URL đến tài liệu đính kèm.
     */
    @Lob
    @Column(columnDefinition = "LONGTEXT", name = "link_url")
    private String linkUrl;

    /**
     * Trạng thái hoạt động của tài liệu (true: hoạt động, false: không hoạt động).
     */
    @Column(name = "is_active")
    private Boolean isActive;

    /**
     * Trạng thái xóa của tài liệu (true: đã xóa, false: chưa xóa).
     */
    @Column(name = "is_delete")
    private Boolean isDelete;

    /**
     * Thời gian tạo tài liệu.
     */
    @Column(name = "created_at")
    private Date createdAt;

    /**
     * Thời gian cập nhật tài liệu.
     */
    @Column(name = "updated_at")
    private Date updatedAt;

    /**
     * Người dùng tạo tài liệu.
     */
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "class_notification")
    private ClassNotification classNotification;

    /**
     * Thiết lập các giá trị mặc định khi tạo mới tài liệu:
     * - isActive = false
     * - isDelete = false
     * - createdAt = thời gian hiện tại
     * - updatedAt = null
     */
    @PrePersist
    protected void onCreate() {
        this.isActive = true;
        this.isDelete = false;
        this.createdAt = TimeUtil.getCurrentTimestamp();
        this.updatedAt = null;
    }

    /**
     * Cập nhật thời gian khi tài liệu được chỉnh sửa.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = TimeUtil.getCurrentTimestamp();
    }
}
