package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.dto.courseobjective.CourseObjectiveResponse;
import com.oss.osscourse.entity.CourseObjective;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseObjectiveMapper extends BaseMapper<CourseObjective> {

    /**
     * 查询课程目标列表（JOIN course 表获取课程编码和名称）。
     * 为兼容已部署环境中的旧表结构，列表查询只读取当前页面必需字段，
     * 不强依赖 created_at / updated_at 这类非核心展示列。
     */
    @Select({
            "<script>",
            "SELECT co.co_id AS coId, co.objective_code AS objectiveCode,",
            "co.co_description AS description, co.course_id AS courseId,",
            "c.course_code AS courseCode, c.course_name AS courseName",
            "FROM course_objective co",
            "LEFT JOIN course c ON co.course_id = c.course_id",
            "<where>",
            "<if test='objectiveCode != null and objectiveCode != \"\"'>",
            "AND co.objective_code LIKE CONCAT('%', #{objectiveCode}, '%')",
            "</if>",
            "<if test='courseId != null'>",
            "AND co.course_id = #{courseId}",
            "</if>",
            "</where>",
            "ORDER BY co.course_id ASC, co.objective_code ASC",
            "</script>"
    })
    List<CourseObjectiveResponse> selectListWithCourse(@Param("objectiveCode") String objectiveCode,
                                                        @Param("courseId") Long courseId);

    /**
     * 按ID查询单个课程目标详情（含原始 co_description 及课程信息）。
     * 同样避免依赖旧环境中可能尚未补齐的时间列。
     */
    @Select({
            "SELECT co.co_id AS coId, co.objective_code AS objectiveCode,",
            "co.co_description AS description, co.course_id AS courseId,",
            "c.course_code AS courseCode, c.course_name AS courseName",
            "FROM course_objective co",
            "LEFT JOIN course c ON co.course_id = c.course_id",
            "WHERE co.co_id = #{coId}"
    })
    CourseObjectiveResponse selectByIdWithCourse(@Param("coId") Long coId);

    /**
     * 统计被考核点引用的数量（删除前置校验）。
     */
    @Select("SELECT COUNT(1) FROM assessment_point WHERE co_id = #{coId}")
    int countAssessmentPointRefs(@Param("coId") Long coId);

    /**
     * 统计被内部权重引用的数量（删除前置校验）。
     */
    @Select("SELECT COUNT(1) FROM objective_indicator_contribution WHERE co_id = #{coId}")
    int countObjectiveIndicatorContributionRefs(@Param("coId") Long coId);
}
