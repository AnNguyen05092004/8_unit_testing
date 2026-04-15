package com.doan2025.webtoeic.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SearchOrderDto {
    private String searchString; // tìm theo ten tac gia, ten khoa hoc
    //    private Long idTeacher;
    private List<String> statusOrder;
    private Date fromDate;
    private Date toDate;
}
