package com.oss.osscourse.service;

import com.oss.osscourse.dto.supportmatrix.AddCourseIndicatorSupportRequest;
import com.oss.osscourse.dto.supportmatrix.CourseIndicatorSupportListRequest;
import com.oss.osscourse.dto.supportmatrix.CourseIndicatorSupportResponse;
import com.oss.osscourse.dto.supportmatrix.DeleteCourseIndicatorSupportRequest;
import com.oss.osscourse.dto.supportmatrix.MatrixAcademicTermResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixCourseOptionResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixGraduationRequirementResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixIndicatorPointResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixMajorOptionResponse;
import com.oss.osscourse.dto.supportmatrix.MatrixRelationResponse;
import com.oss.osscourse.dto.supportmatrix.ResetSupportMatrixRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixGetRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixMajorFilterRequest;
import com.oss.osscourse.dto.supportmatrix.SupportMatrixSaveRequest;
import com.oss.osscourse.dto.supportmatrix.UpdateCourseIndicatorSupportRequest;

import java.util.List;

public interface SupportMatrixService {
    List<MatrixMajorOptionResponse> listMajors(SupportMatrixMajorFilterRequest request,
                                               List<String> roles,
                                               List<String> permissions);

    List<Integer> listGradeYears(SupportMatrixMajorFilterRequest request,
                                 List<String> roles,
                                 List<String> permissions);

    List<MatrixCourseOptionResponse> listCourses(SupportMatrixMajorFilterRequest request,
                                                 List<String> roles,
                                                 List<String> permissions);

    List<MatrixAcademicTermResponse> listAcademicTerms(List<String> roles, List<String> permissions);

    List<MatrixGraduationRequirementResponse> listGraduationRequirements(SupportMatrixMajorFilterRequest request,
                                                                         List<String> roles,
                                                                         List<String> permissions);

    List<MatrixIndicatorPointResponse> listIndicatorPoints(SupportMatrixMajorFilterRequest request,
                                                           List<String> roles,
                                                           List<String> permissions);

    List<MatrixRelationResponse> getSupportMatrix(SupportMatrixGetRequest request,
                                                  List<String> roles,
                                                  List<String> permissions);

    List<CourseIndicatorSupportResponse> listCourseIndicatorSupports(CourseIndicatorSupportListRequest request,
                                                                     List<String> roles,
                                                                     List<String> permissions);

    void addCourseIndicatorSupport(AddCourseIndicatorSupportRequest request,
                                   List<String> roles,
                                   List<String> permissions);

    void updateCourseIndicatorSupport(UpdateCourseIndicatorSupportRequest request,
                                      List<String> roles,
                                      List<String> permissions);

    void deleteCourseIndicatorSupport(DeleteCourseIndicatorSupportRequest request,
                                      List<String> roles,
                                      List<String> permissions);

    void saveSupportMatrix(SupportMatrixSaveRequest request, List<String> roles, List<String> permissions);

    void resetSupportMatrix(ResetSupportMatrixRequest request, List<String> roles, List<String> permissions);
}
