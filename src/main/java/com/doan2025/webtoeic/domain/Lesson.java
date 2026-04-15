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
@Table(name = "lesson")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "title", columnDefinition = "LONGTEXT")
    private String title;

    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Lob
    @Column(name = "video_url", columnDefinition = "LONGTEXT")
    private String videoUrl;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "is_preview_able")
    private Boolean isPreviewAble;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_delete")
    private Boolean isDelete;

    @ManyToOne
    @JoinColumn(name = "course")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    public Lesson(String title, String content, String videoUrl, Double duration, Integer orderIndex, Course course) {
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.orderIndex = orderIndex;
        this.course = course;
    }

    @PrePersist
    protected void onCreate() {
        this.isActive = true;
        this.isDelete = false;
        this.isPreviewAble = false;
        this.createdAt = TimeUtil.getCurrentTimestamp();
        this.updatedAt = null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = TimeUtil.getCurrentTimestamp();
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", duration=" + duration +
                ", orderIndex=" + orderIndex +
                ", isPreviewAble=" + isPreviewAble +
                '}';
    }
}
