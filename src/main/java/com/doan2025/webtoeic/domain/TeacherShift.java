package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.constants.enums.EShiftType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@Table(name = "teacher_shift")
@AllArgsConstructor
@NoArgsConstructor
public class TeacherShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher")
    private User teacher;

    @Column(name = "start_at")
    private Date startAt;

    @Column(name = "end_at")
    private Date endAt;

    @Column(name = "shift_type")
    @Enumerated(EnumType.STRING)
    private EShiftType shiftType;

}
