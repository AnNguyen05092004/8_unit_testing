package com.doan2025.webtoeic.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "student")
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "education", columnDefinition = "LONGTEXT")
    private String education;

    @Lob
    @Column(name = "major", columnDefinition = "LONGTEXT")
    private String major;

    @JsonIgnoreProperties(allowSetters = true, allowGetters = true, value = {"user"})
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    public Student(String education, String major) {
        this.education = education;
        this.major = major;
    }

    @Override
    public String toString() {
        return "Student{" +
                "major='" + major + '\'' +
                ", education='" + education + '\'' +
                ", id=" + id +
                '}';
    }
}
