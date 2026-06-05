package com.oss.osscourse.service;

import com.oss.osscourse.dto.teachercontext.ScoreImportContextResponse;
import com.oss.osscourse.dto.teachercontext.TeacherClassRequest;
import com.oss.osscourse.dto.teachercontext.TeacherClassStudentResponse;
import com.oss.osscourse.dto.teachercontext.TeacherTeachingClassQueryRequest;
import com.oss.osscourse.dto.teachercontext.TeacherTeachingClassResponse;

import java.util.List;

public interface TeacherContextService {
    List<TeacherTeachingClassResponse> listMyTeachingClasses(TeacherTeachingClassQueryRequest request,
                                                             Long userId,
                                                             List<String> roles);

    List<TeacherClassStudentResponse> listMyClassStudents(TeacherClassRequest request,
                                                          Long userId,
                                                          List<String> roles);

    ScoreImportContextResponse getScoreImportContext(TeacherClassRequest request,
                                                     Long userId,
                                                     List<String> roles);
}
