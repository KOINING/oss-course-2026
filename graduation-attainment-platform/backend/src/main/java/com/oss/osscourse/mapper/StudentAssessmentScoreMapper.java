package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.entity.StudentAssessmentScore;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentAssessmentScoreMapper extends BaseMapper<StudentAssessmentScore> {

    @Insert({
            "<script>",
            "INSERT INTO student_assessment_score (student_id, ap_id, class_id, actual_score)",
            "VALUES",
            "<foreach collection='scores' item='score' separator=','>",
            "(#{score.studentId}, #{score.apId}, #{score.classId}, #{score.actualScore})",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE actual_score = VALUES(actual_score)",
            "</script>"
    })
    void upsertBatch(@Param("scores") List<StudentAssessmentScore> scores);
}
