package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    @Select("""
            SELECT DISTINCT enrollment_year
            FROM student
            WHERE enrollment_year IS NOT NULL
            ORDER BY enrollment_year DESC
            """)
    List<Integer> selectEnrollmentYears();
}
