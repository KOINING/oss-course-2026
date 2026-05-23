package com.oss.osscourse.service;

import com.oss.osscourse.dto.major.*;

import java.util.List;

public interface MajorService {

    List<MajorResponse> listMajors(MajorQueryRequest request);

    List<MajorResponse> listMajorsForSelect();

    MajorResponse getMajorById(Long majorId);

    void createMajor(MajorCreateRequest request);

    void updateMajor(MajorUpdateRequest request);

    void saveMajor(MajorSaveRequest request);

    void updateMajorStatus(MajorStatusRequest request);

    void deleteMajor(Long majorId);
}
