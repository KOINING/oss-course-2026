package com.oss.osscourse.dto.trace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "专业级到原始成绩逐层追溯台账行")
public class AchievementLedgerRow {
    private Long majorId;
    private String majorName;
    private Integer gradeYear;
    private Long termId;
    private String termCode;
    private Long grId;
    private String grCode;
    private String grDescription;
    private Long ipId;
    private String ipCode;
    private String ipDescription;
    private Float finalAchievement;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Long classId;
    private String classCode;
    private String className;
    private String calcStatus;
    private Float courseIndicatorAchievement;
    private Float macroWeight;
    private Long coId;
    private String objectiveCode;
    private String coDescription;
    private Float objectiveAchievement;
    private Float internalWeight;
    private Long apId;
    private String apName;
    private Float fullScore;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Float actualScore;
}
