package com.oss.osscourse.dto.teachingclass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingClassStudentCountResponse {

    private Long classId;

    private Long studentCount;
}
