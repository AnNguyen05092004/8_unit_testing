package com.doan2025.webtoeic.dto.response;

import com.doan2025.webtoeic.constants.enums.EGender;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
//@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dob;
    private String gender;
    private String avatarUrl;
    private Boolean isActive;
    private Boolean isDelete;
    private String education;
    private String major;
    private String role;
    private StudentResponse student;
    private ConsultantResponse consultant;
    private TeacherResponse teacher;
    private ManagerResponse manager;
    private Date createdAt;
    private Date updatedAt;
    private String code;

    public UserResponse(Long id, String firstName, String lastName, String phone, String address,
                        Date dob, EGender gender, String avatarUrl, Boolean isActive, Boolean isDelete,
                        String education, String major, ERole role, Date createdAt, Date updatedAt, String code) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
        this.gender = gender != null ? gender.name() : null;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
        this.isDelete = isDelete;
        this.education = education;
        this.major = major;
        this.role = role != null ? role.name() : null;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.code = code;
    }

    public UserResponse(String firstName, String lastName, String phone, String address, Date dob,
                        EGender gender, String avatarUrl, Boolean isActive, Boolean isDelete,
                        String education, String major, Date createdAt, Date updatedAt, String code) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
        this.gender = gender != null ? gender.name() : null;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
        this.isDelete = isDelete;
        this.education = education;
        this.major = major;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.code = code;
    }
}
