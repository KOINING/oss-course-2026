package com.oss.osscourse.dto.basic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CollegeSaveRequest {
    private Long collegeId;

    @NotBlank(message = "学院编码不能为空")
    @Size(max = 20, message = "学院编码长度不能超过20")
    private String collegeCode;

    @NotBlank(message = "学院名称不能为空")
    @Size(max = 100, message = "学院名称长度不能超过100")
    private String collegeName;
}
