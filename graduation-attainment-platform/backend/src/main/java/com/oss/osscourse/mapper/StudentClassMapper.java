package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.dto.teachercontext.TeacherClassStudentResponse;
import com.oss.osscourse.entity.StudentClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentClassMapper extends BaseMapper<StudentClass> {
    @Select({
            "SELECT sc.sc_id AS scId, s.student_id AS studentId, s.student_no AS studentNo,",
            "s.student_name AS studentName, s.major_id AS majorId, m.major_name AS majorName,",
            "s.enrollment_year AS enrollmentYear, s.status AS status",
            "FROM student_class sc",
            "JOIN student s ON s.student_id = sc.student_id",
            "LEFT JOIN major m ON m.major_id = s.major_id",
            "WHERE sc.class_id = #{classId}",
            "ORDER BY s.student_no ASC"
    })
    List<TeacherClassStudentResponse> selectStudentsByClassId(@Param("classId") Long classId);
}
