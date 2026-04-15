package com.doan2025.webtoeic.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "teacher")
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "education", columnDefinition = "LONGTEXT")
    private String education;

    @Lob
    @Column(name = "degree", columnDefinition = "LONGTEXT")
    private String degree;

    @JsonIgnoreProperties(allowSetters = true, allowGetters = true, value = {"user"})
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    public Teacher(String education, String degree) {
        this.education = education;
        this.degree = degree;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "degree='" + degree + '\'' +
                ", education='" + education + '\'' +
                ", id=" + id +
                '}';
    }
}
