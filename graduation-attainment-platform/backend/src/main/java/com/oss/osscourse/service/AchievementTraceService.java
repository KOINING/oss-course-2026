package com.oss.osscourse.service;

import com.oss.osscourse.dto.trace.CourseToObjectiveTraceRequest;
import com.oss.osscourse.dto.trace.CourseToObjectiveTraceResponse;
import com.oss.osscourse.dto.trace.MajorToCourseTraceRequest;
import com.oss.osscourse.dto.trace.MajorToCourseTraceResponse;
import com.oss.osscourse.dto.trace.ObjectiveToScoreTraceRequest;
import com.oss.osscourse.dto.trace.ObjectiveToScoreTraceResponse;

import java.util.List;

public interface AchievementTraceService {
    List<MajorToCourseTraceResponse> getMajorToCourseTrace(MajorToCourseTraceRequest request);

    CourseToObjectiveTraceResponse getCourseToObjectiveTrace(CourseToObjectiveTraceRequest request);

    ObjectiveToScoreTraceResponse getObjectiveToScoreTrace(ObjectiveToScoreTraceRequest request);

    byte[] exportAchievementLedger(MajorToCourseTraceRequest request);
}
