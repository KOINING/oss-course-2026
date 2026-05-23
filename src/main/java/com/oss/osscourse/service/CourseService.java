package com.oss.osscourse.service;

import com.oss.osscourse.dto.course.*;

import java.util.List;

public interface CourseService {
    List<CourseVO> listCourses(CourseQueryRequest request);

    CourseVO getCourse(Long courseId);

    void saveCourse(CourseSaveRequest request);

    void updateCourseStatus(CourseStatusRequest request);

    void deleteCourse(Long courseId);
}
