package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.utils.TimeUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@Table(name = "enrollment")
@AllArgsConstructor
@NoArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "progress")
    private BigDecimal progress;

    @Column(name = "enrolled_at")
    private Date enrolledAt;

    @Column(name = "completed_at")
    private Date completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course")
    private Course course;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LessonCompletion> completedLessons;

    @PrePersist
    protected void onCreate() {
        this.enrolledAt = TimeUtil.getCurrentTimestamp();
        this.progress = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onComplete() {
        if (progress.compareTo(BigDecimal.valueOf(100)) == 0) {
            this.completedAt = TimeUtil.getCurrentTimestamp();
        }
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", progress=" + progress +
                ", enrolledAt=" + enrolledAt +
                ", completedAt=" + completedAt +
                '}';
    }
}
