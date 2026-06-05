package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.dto.assessmentpoint.AssessmentPointResponse;
import com.oss.osscourse.entity.AssessmentPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentPointMapper extends BaseMapper<AssessmentPoint> {

    /**
     * 查询考核点列表，JOIN 课程目标和课程表获取上下文信息。
     * 支持按考核点名称（模糊）、所属课程ID、所属课程目标ID 筛选。
     */
    @Select({
            "<script>",
            "SELECT ap.ap_id AS apId, ap.ap_name AS apName, ap.full_score AS fullScore,",
            "ap.co_id AS coId, co.objective_code AS objectiveCode, co.co_description AS coDescription,",
            "c.course_id AS courseId, c.course_code AS courseCode, c.course_name AS courseName,",
            "ap.created_at AS createdAt, ap.updated_at AS updatedAt",
            "FROM assessment_point ap",
            "JOIN course_objective co ON ap.co_id = co.co_id",
            "JOIN course c ON co.course_id = c.course_id",
            "<where>",
            "<if test='apName != null and apName != \"\"'>",
            "AND ap.ap_name LIKE CONCAT('%', #{apName}, '%')",
            "</if>",
            "<if test='courseId != null'>",
            "AND co.course_id = #{courseId}",
            "</if>",
            "<if test='coId != null'>",
            "AND ap.co_id = #{coId}",
            "</if>",
            "</where>",
            "ORDER BY c.course_code ASC, co.objective_code ASC, ap.ap_name ASC",
            "</script>"
    })
    List<AssessmentPointResponse> selectListWithDetails(@Param("apName") String apName,
                                                         @Param("courseId") Long courseId,
                                                         @Param("coId") Long coId);

    /**
     * 按ID查询单个考核点详情（含课程目标和课程信息）。
     */
    @Select({
            "SELECT ap.ap_id AS apId, ap.ap_name AS apName, ap.full_score AS fullScore,",
            "ap.co_id AS coId, co.objective_code AS objectiveCode, co.co_description AS coDescription,",
            "c.course_id AS courseId, c.course_code AS courseCode, c.course_name AS courseName,",
            "ap.created_at AS createdAt, ap.updated_at AS updatedAt",
            "FROM assessment_point ap",
            "JOIN course_objective co ON ap.co_id = co.co_id",
            "JOIN course c ON co.course_id = c.course_id",
            "WHERE ap.ap_id = #{apId}"
    })
    AssessmentPointResponse selectByIdWithDetails(@Param("apId") Long apId);

    /**
     * 统计同一课程下相同名称的考核点数量（用于名称唯一性校验）。
     */
    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM assessment_point ap",
            "JOIN course_objective co ON ap.co_id = co.co_id",
            "WHERE co.course_id = #{courseId}",
            "AND ap.ap_name = #{apName}",
            "<if test='excludeApId != null'>",
            "AND ap.ap_id != #{excludeApId}",
            "</if>",
            "</script>"
    })
    int countByNameInCourse(@Param("courseId") Long courseId,
                            @Param("apName") String apName,
                            @Param("excludeApId") Long excludeApId);

    /**
     * 统计被学生成绩引用的数量（删除前置校验）。
     */
    @Select("SELECT COUNT(1) FROM student_assessment_score WHERE ap_id = #{apId}")
    int countScoreRefs(@Param("apId") Long apId);
}
