package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.dto.objectivecontribution.ObjectiveIndicatorContributionResponse;
import com.oss.osscourse.entity.ObjectiveIndicatorContribution;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ObjectiveIndicatorContributionMapper extends BaseMapper<ObjectiveIndicatorContribution> {

    /**
     * 按课程 + 专业 + 年级查询当前已配置的内部权重，JOIN 获取课程目标、指标点、毕业要求信息。
     * 仅返回其 ipId 对应的毕业要求匹配目标专业年级的记录。
     */
    @Select({
            "<script>",
            "SELECT",
            "  oic.oic_id AS oicId,",
            "  co.co_id AS coId,",
            "  co.objective_code AS objectiveCode,",
            "  co.co_description AS coDescription,",
            "  supported.ipId AS ipId,",
            "  supported.ipCode AS ipCode,",
            "  supported.ipDescription AS ipDescription,",
            "  supported.grId AS grId,",
            "  supported.grCode AS grCode,",
            "  supported.grDescription AS grDescription,",
            "  oic.internal_weight AS internalWeight,",
            "  oic.created_at AS createdAt",
            "FROM course_objective co",
            "JOIN (",
            "  SELECT DISTINCT",
            "    ip.ip_id AS ipId,",
            "    ip.ip_code AS ipCode,",
            "    ip.ip_description AS ipDescription,",
            "    gr.gr_id AS grId,",
            "    gr.gr_code AS grCode,",
            "    gr.gr_description AS grDescription",
            "  FROM course_indicator_support cis",
            "  JOIN indicator_point ip ON ip.ip_id = cis.ip_id",
            "  JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id",
            "  JOIN course_major cm",
            "    ON cm.course_id = cis.course_id",
            "   AND cm.major_id = gr.major_id",
            "   AND cm.grade_year = gr.grade_year",
            "  WHERE cis.course_id = #{courseId}",
            "    AND gr.major_id = #{majorId}",
            "    AND gr.grade_year = #{gradeYear}",
            ") supported ON 1 = 1",
            "LEFT JOIN objective_indicator_contribution oic",
            "  ON oic.co_id = co.co_id",
            " AND oic.ip_id = supported.ipId",
            "WHERE co.course_id = #{courseId}",
            "ORDER BY supported.grCode ASC, supported.ipCode ASC, co.objective_code ASC",
            "</script>"
    })
    List<ObjectiveIndicatorContributionResponse> selectByCourseAndProgram(@Param("courseId") Long courseId,
                                                                          @Param("majorId") Long majorId,
                                                                          @Param("gradeYear") Integer gradeYear);

    /**
     * 删除属于指定课程 + 专业 + 年级版本的所有内部权重记录。
     * 仅删除 ipId 对应毕业要求专业年级匹配的记录，避免误删其他专业或年级版本的配置。
     * 用于批量保存前的"先删后插"策略。
     */
    @Delete({
            "DELETE FROM objective_indicator_contribution",
            "WHERE co_id IN (SELECT co_id FROM course_objective WHERE course_id = #{courseId})",
            "AND ip_id IN (",
            "  SELECT ip.ip_id FROM indicator_point ip",
            "  JOIN graduation_requirement gr ON ip.gr_id = gr.gr_id",
            "  WHERE gr.major_id = #{majorId}",
            "    AND gr.grade_year = #{gradeYear}",
            ")"
    })
    int deleteByCourseIdAndProgram(@Param("courseId") Long courseId,
                                   @Param("majorId") Long majorId,
                                   @Param("gradeYear") Integer gradeYear);

    /**
     * 查询该课程在指定专业年级版本下所有合法的指标点ID。
     * 合法性：ip → gr → major_id + grade_year，且课程绑定该专业年级。
     */
    @Select({
            "SELECT ip.ip_id",
            "FROM indicator_point ip",
            "JOIN graduation_requirement gr ON ip.gr_id = gr.gr_id",
            "JOIN course_major cm ON cm.major_id = gr.major_id",
            "WHERE cm.course_id = #{courseId}",
            "AND cm.grade_year = gr.grade_year",
            "AND gr.major_id = #{majorId}",
            "AND gr.grade_year = #{gradeYear}"
    })
    List<Long> selectValidIpIds(@Param("courseId") Long courseId,
                                @Param("majorId") Long majorId,
                                @Param("gradeYear") Integer gradeYear);

    @Select({
            "<script>",
            "SELECT oic.*",
            "FROM objective_indicator_contribution oic",
            "JOIN indicator_point ip ON ip.ip_id = oic.ip_id",
            "JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id",
            "WHERE oic.co_id IN",
            "<foreach collection='coIds' item='coId' open='(' separator=',' close=')'>",
            "  #{coId}",
            "</foreach>",
            "AND gr.major_id = #{majorId}",
            "AND gr.grade_year = #{gradeYear}",
            "</script>"
    })
    List<ObjectiveIndicatorContribution> selectByObjectiveIdsAndContext(@Param("coIds") List<Long> coIds,
                                                                        @Param("majorId") Long majorId,
                                                                        @Param("gradeYear") Integer gradeYear);
}
