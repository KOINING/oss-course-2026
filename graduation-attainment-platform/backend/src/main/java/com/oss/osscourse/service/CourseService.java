package com.oss.osscourse.service;

import com.oss.osscourse.dto.course.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {

    List<CourseResponse> listCourses(CourseQueryRequest request);

    CourseResponse getCourseById(Long courseId);

    void createCourse(CourseCreateRequest request);

    void updateCourse(CourseUpdateRequest request);

    void saveCourse(CourseSaveRequest request);

    void updateCourseStatus(CourseStatusRequest request);

    void deleteCourse(Long courseId);

    CourseImportResult importCourses(MultipartFile file);

    List<Integer> listGradeYears();
}
