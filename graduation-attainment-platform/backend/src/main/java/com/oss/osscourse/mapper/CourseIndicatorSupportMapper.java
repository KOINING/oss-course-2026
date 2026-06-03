package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.dto.supportmatrix.CourseIndicatorSupportResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixCourseOptionResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixGraduationRequirementResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixIndicatorPointResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixMajorOptionResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixRelationResponse;
import com.oss.osscourse.entity.CourseIndicatorSupport;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseIndicatorSupportMapper extends BaseMapper<CourseIndicatorSupport> {
    @Select({
            "<script>",
            "SELECT m.major_id AS majorId, m.major_code AS majorCode, m.major_name AS majorName, m.status AS status",
            "FROM major m",
            "<where>",
            "  <if test='majorId != null'>AND m.major_id = #{majorId}</if>",
            "  AND m.status = 1",
            "</where>",
            "ORDER BY m.major_code ASC",
            "</script>"
    })
    List<MatrixMajorOptionResponse> selectMajorOptions(@Param("majorId") Long majorId);

    @Select({
            "<script>",
            "SELECT c.course_id AS courseId, c.course_code AS courseCode, c.course_name AS courseName, c.status AS status,",
            "cm.grade_year AS gradeYear",
            "FROM course c",
            "JOIN course_major cm ON cm.course_id = c.course_id",
            "WHERE cm.major_id = #{majorId}",
            "AND cm.grade_year = #{gradeYear}",
            "AND c.status = 1",
            "ORDER BY c.course_code ASC, cm.grade_year DESC",
            "</script>"
    })
    List<MatrixCourseOptionResponse> selectCourseOptionsByMajor(@Param("majorId") Long majorId,
                                                                @Param("gradeYear") Integer gradeYear);

    @Select({
            "<script>",
            "SELECT gr.gr_id AS grId, gr.gr_code AS grCode, gr.gr_description AS grDescription,",
            "gr.major_id AS majorId, gr.grade_year AS gradeYear, gr.status AS status",
            "FROM graduation_requirement gr",
            "WHERE gr.major_id = #{majorId}",
            "AND gr.grade_year = #{gradeYear}",
            "AND gr.status = 1",
            "ORDER BY gr.gr_code ASC",
            "</script>"
    })
    List<MatrixGraduationRequirementResponse> selectGraduationRequirementsByMajor(@Param("majorId") Long majorId,
                                                                                  @Param("gradeYear") Integer gradeYear);

    @Select({
            "<script>",
            "SELECT ip.ip_id AS ipId, ip.ip_code AS ipCode, ip.ip_description AS ipDescription,",
            "ip.gr_id AS grId, gr.gr_code AS grCode, gr.gr_description AS grDescription, gr.grade_year AS gradeYear, ip.status AS status",
            "FROM indicator_point ip",
            "JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id",
            "WHERE gr.major_id = #{majorId}",
            "AND gr.grade_year = #{gradeYear}",
            "AND gr.status = 1",
            "AND ip.status = 1",
            "ORDER BY gr.gr_code ASC, ip.ip_code ASC",
            "</script>"
    })
    List<MatrixIndicatorPointResponse> selectIndicatorPointsByMajor(@Param("majorId") Long majorId,
                                                                    @Param("gradeYear") Integer gradeYear);

    @Select({
            "<script>",
            "SELECT cis.cis_id AS cisId, cis.course_id AS courseId, cis.ip_id AS ipId,",
            "cis.total_weight AS totalWeight, cis.total_weight AS weight",
            "FROM course_indicator_support cis",
            "JOIN indicator_point ip ON ip.ip_id = cis.ip_id",
            "JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id",
            "JOIN course_major cm ON cm.course_id = cis.course_id",
            "WHERE gr.major_id = #{majorId}",
            "AND gr.grade_year = #{gradeYear}",
            "AND cm.major_id = #{majorId}",
            "AND cm.grade_year = #{gradeYear}",
            "ORDER BY cis.cis_id ASC",
            "</script>"
    })
    List<MatrixRelationResponse> selectMatrixRelationsByMajor(@Param("majorId") Long majorId,
                                                              @Param("gradeYear") Integer gradeYear);

    @Select({
            "<script>",
            "SELECT cis.cis_id AS cisId, cis.course_id AS courseId, c.course_code AS courseCode, c.course_name AS courseName, c.status AS courseStatus,",
            "cis.ip_id AS ipId, ip.ip_code AS ipCode, ip.ip_description AS ipDescription, ip.status AS ipStatus,",
            "gr.gr_id AS grId, gr.gr_code AS grCode, gr.gr_description AS grDescription, gr.major_id AS majorId, gr.grade_year AS gradeYear,",
            "cis.total_weight AS totalWeight",
            "FROM course_indicator_support cis",
            "JOIN course c ON c.course_id = cis.course_id",
            "JOIN indicator_point ip ON ip.ip_id = cis.ip_id",
            "JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id",
            "<where>",
            "  <if test='majorId != null'>AND gr.major_id = #{majorId}</if>",
            "  <if test='gradeYear != null'>AND gr.grade_year = #{gradeYear}</if>",
            "  <if test='courseId != null'>AND cis.course_id = #{courseId}</if>",
            "  <if test='ipId != null'>AND cis.ip_id = #{ipId}</if>",
            "</where>",
            "ORDER BY gr.grade_year DESC, gr.gr_code ASC, ip.ip_code ASC, c.course_code ASC",
            "</script>"
    })
    List<CourseIndicatorSupportResponse> selectCourseIndicatorSupports(@Param("majorId") Long majorId,
                                                                       @Param("gradeYear") Integer gradeYear,
                                                                       @Param("courseId") Long courseId,
                                                                       @Param("ipId") Long ipId);

    @Delete({
            "<script>",
            "DELETE cis FROM course_indicator_support cis",
            "JOIN indicator_point ip ON ip.ip_id = cis.ip_id",
            "JOIN graduation_requirement gr ON gr.gr_id = ip.gr_id",
            "WHERE gr.major_id = #{majorId}",
            "AND gr.grade_year = #{gradeYear}",
            "</script>"
    })
    int deleteByMajorId(@Param("majorId") Long majorId, @Param("gradeYear") Integer gradeYear);

    @Select({
            "<script>",
            "SELECT DISTINCT grade_year FROM (",
            "SELECT gr.grade_year AS grade_year FROM graduation_requirement gr",
            "<if test='majorId != null'>WHERE gr.major_id = #{majorId}</if>",
            "UNION",
            "SELECT cm.grade_year AS grade_year FROM course_major cm",
            "<if test='majorId != null'>WHERE cm.major_id = #{majorId}</if>",
            ") years",
            "ORDER BY grade_year DESC",
            "</script>"
    })
    List<Integer> selectGradeYears(@Param("majorId") Long majorId);
}
