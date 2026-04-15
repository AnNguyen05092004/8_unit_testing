package com.doan2025.webtoeic.domain;

import com.doan2025.webtoeic.constants.enums.EGender;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.utils.TimeUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Lob
    @Column(name = "password", nullable = false, columnDefinition = "LONGTEXT")
    private String password;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Lob
    @Column(name = "first_name", columnDefinition = "LONGTEXT")
    private String firstName;

    @Lob
    @Column(name = "last_name", columnDefinition = "LONGTEXT")
    private String lastName;

    @Lob
    @Column(name = "phone", columnDefinition = "LONGTEXT")
    private String phone;

    @Lob
    @Column(name = "address", columnDefinition = "LONGTEXT")
    private String address;

    @Column(name = "dob")
    private Date dob;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private EGender gender;

    @Lob
    @Column(name = "avatar_url", columnDefinition = "LONGTEXT")
    private String avatarUrl;

    @Column(name = "is_delete")
    private Boolean isDelete;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private ERole role;

    @JsonIgnoreProperties(allowSetters = true, allowGetters = true)
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Manager manager;

    @JsonIgnoreProperties(allowSetters = true, allowGetters = true)
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Consultant consultant;

    @JsonIgnoreProperties(allowSetters = true, allowGetters = true)
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Teacher teacher;

    @JsonIgnoreProperties(allowSetters = true, allowGetters = true)
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Student student;

    @OneToOne(mappedBy = "user")
    private ForgotPassword forgotPassword;

    public User(String email, String password, String firstName, String lastName, ERole role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        this.isActive = true;
        this.isDelete = false;
        this.createdAt = TimeUtil.getCurrentTimestamp();
        this.updatedAt = null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = TimeUtil.getCurrentTimestamp();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", dob=" + dob +
                ", gender=" + gender +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}
