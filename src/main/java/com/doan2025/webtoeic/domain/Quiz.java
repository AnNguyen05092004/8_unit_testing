package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.constants.enums.EQuizStatus;
import com.doan2025.webtoeic.utils.TimeUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@Table(name = "quiz")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "title", columnDefinition = "LONGTEXT")
    private String title;

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "total_questions")
    private Long totalQuestions;

    @Column(name = "is_student_created")
    private Boolean isStudentCreated;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EQuizStatus status;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_delete")
    private Boolean isDelete;

    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "update_at")
    private Date updateAt;

    @ManyToOne
    @JoinColumn(name = "create_by")
    private User createBy;

    @ManyToOne
    @JoinColumn(name = "update_by")
    private User updateBy;

    @PrePersist
    protected void onCreate() {
        this.isActive = true;
        this.isDelete = false;
        this.createAt = TimeUtil.getCurrentTimestamp();
        this.updateAt = null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = TimeUtil.getCurrentTimestamp();
    }
}
