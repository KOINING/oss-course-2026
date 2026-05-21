package com.oss.osscourse.service;

import com.oss.osscourse.dto.basic.*;

import java.util.List;

public interface BasicDataService {

    List<CollegeResponse> listColleges(CollegeQueryRequest request, List<String> roles);

    void addCollege(CollegeSaveRequest request, List<String> roles);

    void updateCollege(CollegeSaveRequest request, List<String> roles);

    void deleteCollege(CollegeDeleteRequest request, List<String> roles);

    List<AcademicTermResponse> listAcademicTerms(AcademicTermQueryRequest request, List<String> roles);

    void addAcademicTerm(AcademicTermSaveRequest request, List<String> roles);

    void updateAcademicTerm(AcademicTermSaveRequest request, List<String> roles);

    void deleteAcademicTerm(AcademicTermDeleteRequest request, List<String> roles);
}
