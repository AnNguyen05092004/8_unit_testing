package com.doan2025.webtoeic.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "category_course")
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Override
    public String toString() {
        return "CategoryCourse{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
