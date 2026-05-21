package com.oss.osscourse.dto.basic;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CollegeResponse {
    private Long collegeId;
    private String collegeCode;
    private String collegeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
