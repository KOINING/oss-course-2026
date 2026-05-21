package com.oss.osscourse.dto.basic;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AcademicTermResponse {
    private Long termId;
    private String termCode;
    private Integer academicYear;
    private Integer semester;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
