package com.oss.osscourse.service;

import com.oss.osscourse.dto.major.*;

import java.util.List;

public interface MajorService {
    List<MajorVO> listMajors(MajorQueryRequest request);

    MajorVO getMajor(Long majorId);

    void saveMajor(MajorSaveRequest request);

    void updateMajorStatus(MajorStatusRequest request);

    void deleteMajor(Long majorId);
}
