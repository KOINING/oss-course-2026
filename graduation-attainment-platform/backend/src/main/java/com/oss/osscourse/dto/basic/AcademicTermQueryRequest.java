package com.oss.osscourse.dto.basic;

import lombok.Data;

@Data
public class AcademicTermQueryRequest {
    private String termCode;
    private Integer academicYear;
    private Integer semester;
}
