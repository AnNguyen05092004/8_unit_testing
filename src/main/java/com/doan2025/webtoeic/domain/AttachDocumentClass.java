package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.utils.TimeUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entity đại diện cho bảng lưu trữ các tài liệu đính kèm trong thông tin của lớp học.
 * Được sử dụng để quản lý liên kết đến tài liệu (URL), trạng thái hoạt động, trạng thái xóa,
 * và thông tin về người tạo/cập nhật tài liệu.
 */
@Entity
@Data
@Table(name = "attach_document_class")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachDocumentClass {
    /**
     * ID duy nhất của tài liệu đính kèm.
     */
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

    /**
     * Người dùng cập nhật tài liệu.
     */
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

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
        this.isActive = false;
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
