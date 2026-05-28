package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    @Select("SELECT COUNT(1) FROM teaching_class WHERE course_id = #{courseId}")
    Long countTeachingClassReferences(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(1) FROM course_indicator_support WHERE course_id = #{courseId}")
    Long countIndicatorSupportReferences(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(1) FROM course_objective WHERE course_id = #{courseId}")
    Long countCourseObjectiveReferences(@Param("courseId") Long courseId);
}
