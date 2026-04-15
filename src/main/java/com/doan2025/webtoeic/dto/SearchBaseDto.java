package com.doan2025.webtoeic.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SearchBaseDto {
    private String searchString;
    private String email;
    private String name;
    private String phone;
    private Boolean isActive;
    private Boolean isDelete;
    private List<String> userRoles;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date fromDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date toDate;
    private String title;
    private List<String> categoryPost;
    private List<String> categories;
    private Long id;
}
