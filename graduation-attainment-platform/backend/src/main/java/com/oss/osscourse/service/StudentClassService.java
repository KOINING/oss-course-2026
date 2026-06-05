package com.oss.osscourse.service;

import com.oss.osscourse.dto.teachingclass.StudentClassImportResult;
import com.oss.osscourse.entity.StudentClass;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentClassService {

    StudentClassImportResult importStudentClasses(MultipartFile file);

    List<StudentClass> listByTeachingClassId(Long teachingClassId);

    List<StudentClass> listByStudentId(Long studentId);

    void removeStudentFromClass(Long scId);
}
