package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.dto.teachercontext.TeacherTeachingClassResponse;
import com.oss.osscourse.entity.TeachingClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeachingClassMapper extends BaseMapper<TeachingClass> {
    @Select({
            "<script>",
            "SELECT",
            "m.major_id AS majorId, m.major_name AS majorName, tc.grade_year AS gradeYear,",
            "c.course_id AS courseId, c.course_code AS courseCode, c.course_name AS courseName,",
            "tc.class_id AS classId, tc.class_code AS classCode, tc.class_name AS className,",
            "at.term_id AS termId, at.term_code AS termCode, tc.calc_status AS calcStatus",
            "FROM teaching_class tc",
            "JOIN course c ON c.course_id = tc.course_id",
            "LEFT JOIN academic_term at ON at.term_id = tc.term_id",
            "JOIN teacher t ON t.id = tc.teacher_id",
            "LEFT JOIN major m ON m.major_id = t.major_id",
            "WHERE tc.teacher_id = #{teacherId}",
            "<if test='courseId != null'>AND tc.course_id = #{courseId}</if>",
            "<if test='termId != null'>AND tc.term_id = #{termId}</if>",
            "<if test='gradeYear != null'>AND tc.grade_year = #{gradeYear}</if>",
            "<if test='classCode != null and classCode != \"\"'>AND tc.class_code LIKE CONCAT('%', #{classCode}, '%')</if>",
            "<if test='calcStatus != null and calcStatus != \"\"'>AND tc.calc_status = #{calcStatus}</if>",
            "ORDER BY at.term_code DESC, c.course_code ASC, tc.class_code ASC",
            "</script>"
    })
    List<TeacherTeachingClassResponse> selectTeacherTeachingClasses(@Param("teacherId") Long teacherId,
                                                                    @Param("courseId") Long courseId,
                                                                    @Param("termId") Long termId,
                                                                    @Param("gradeYear") Integer gradeYear,
                                                                    @Param("classCode") String classCode,
                                                                    @Param("calcStatus") String calcStatus);

    @Select({
            "SELECT",
            "m.major_id AS majorId, m.major_name AS majorName, tc.grade_year AS gradeYear,",
            "c.course_id AS courseId, c.course_code AS courseCode, c.course_name AS courseName,",
            "tc.class_id AS classId, tc.class_code AS classCode, tc.class_name AS className,",
            "at.term_id AS termId, at.term_code AS termCode, tc.calc_status AS calcStatus",
            "FROM teaching_class tc",
            "JOIN course c ON c.course_id = tc.course_id",
            "LEFT JOIN academic_term at ON at.term_id = tc.term_id",
            "JOIN teacher t ON t.id = tc.teacher_id",
            "LEFT JOIN major m ON m.major_id = t.major_id",
            "WHERE tc.class_id = #{classId}"
    })
    TeacherTeachingClassResponse selectTeachingClassContext(@Param("classId") Long classId);
}
