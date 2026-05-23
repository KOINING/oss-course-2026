package com.oss.osscourse.dto.requirement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraduationRequirementResponse {
    private Long grId;
    private String grCode;
    private String grDescription;
    private Long majorId;
    private String majorName;
}
