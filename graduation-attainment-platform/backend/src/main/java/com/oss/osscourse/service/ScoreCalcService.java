package com.oss.osscourse.service;

import com.oss.osscourse.dto.achievement.CourseCalcRequest;
import com.oss.osscourse.dto.achievement.CourseCalcResponse;
import com.oss.osscourse.dto.achievement.CourseCalcStatusResponse;
import com.oss.osscourse.dto.achievement.MajorCalcRequest;
import com.oss.osscourse.dto.achievement.MajorCalcResponse;
import com.oss.osscourse.dto.score.ScoreImportPreviewResponse;
import com.oss.osscourse.dto.score.ScoreImportRequest;
import com.oss.osscourse.dto.score.ScoreSaveRequest;
import com.oss.osscourse.dto.score.ScoreTemplatePreviewResponse;

public interface ScoreCalcService {

    ScoreTemplatePreviewResponse previewTemplate(Long classId);

    byte[] downloadTemplate(Long classId);

    ScoreImportPreviewResponse importScorePreview(ScoreImportRequest request);

    void saveScores(ScoreSaveRequest request);

    CourseCalcResponse calcCourseAchievement(CourseCalcRequest request);

    MajorCalcResponse calcMajorAchievement(MajorCalcRequest request);

    CourseCalcStatusResponse getCourseCalcStatus(Long majorId, Long termId);
}
