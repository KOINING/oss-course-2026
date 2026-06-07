package com.oss.osscourse.service;

import com.oss.osscourse.dto.achievement.AssessmentFilterOptionsResponse;
import com.oss.osscourse.dto.achievement.MacroDashboardRequest;
import com.oss.osscourse.dto.achievement.MacroDashboardResponse;
import com.oss.osscourse.dto.achievement.MajorCalcResultResponse;
import com.oss.osscourse.dto.achievement.UnlockRequestApproveRequest;

public interface AssessmentQueryService {
    AssessmentFilterOptionsResponse listMajorGradeYearTerms();

    MacroDashboardResponse getMacroDashboard(MacroDashboardRequest request);

    MajorCalcResultResponse getMajorCalcResult(MacroDashboardRequest request);

    void approveUnlock(UnlockRequestApproveRequest request, Long userId);
}
