package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.constants.enums.EJoinStatus;
import com.doan2025.webtoeic.constants.enums.ERole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entity đại diện cho bảng thành viên lớp học (class_member) trong hệ thống.
 * Lưu trữ thông tin về mối quan hệ giữa học sinh hoặc giáo viên với một lớp học,
 * bao gồm trạng thái tham gia, vai trò trong lớp, ngày tham gia, và liên kết với lớp học và người dùng.
 */

@Entity
@Data
@Table(name = "class_member")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassMember {
    /**
     * ID duy nhất của bản ghi thành viên lớp học.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Trạng thái tham gia của thành viên trong lớp (ví dụ: ACTIVE, PENDING).
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EJoinStatus status;

    /**
     * Lớp học mà thành viên tham gia, liên kết với bảng Class.
     */
    @ManyToOne
    @JoinColumn(name = "class")
    private Class clazz;

    /**
     * Người dùng là thành viên của lớp, liên kết với bảng User.
     */
    @ManyToOne
    @JoinColumn(name = "member")
    private User member;

    /**
     * Ngày tham gia lớp học của thành viên.
     */
    @Column
    private Date joinDate;

    /**
     * Vai trò của thành viên trong lớp học (ví dụ: STUDENT, TEACHER).
     */
    @Column(name = "role_in_class")
    @Enumerated(EnumType.STRING)
    private ERole roleInClass;
}
