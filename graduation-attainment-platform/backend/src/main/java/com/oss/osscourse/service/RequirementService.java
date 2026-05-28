package com.oss.osscourse.service;

import com.oss.osscourse.dto.requirement.*;
import com.oss.osscourse.entity.Major;

import java.util.List;

public interface RequirementService {
    List<GraduationRequirementResponse> listGraduationRequirements(GraduationRequirementQueryRequest request,
                                                                  List<String> roles,
                                                                  List<String> permissions);

    void addGraduationRequirement(AddGraduationRequirementRequest request,
                                  List<String> roles,
                                  List<String> permissions);

    void updateGraduationRequirement(UpdateGraduationRequirementRequest request,
                                     List<String> roles,
                                     List<String> permissions);

    void deleteGraduationRequirement(Long grId,
                                     List<String> roles,
                                     List<String> permissions);

    List<IndicatorPointResponse> listIndicatorPoints(IndicatorPointQueryRequest request,
                                                     List<String> roles,
                                                     List<String> permissions);

    void addIndicatorPoint(AddIndicatorPointRequest request,
                           List<String> roles,
                           List<String> permissions);

    void updateIndicatorPoint(UpdateIndicatorPointRequest request,
                              List<String> roles,
                              List<String> permissions);

    void deleteIndicatorPoint(Long ipId,
                              List<String> roles,
                              List<String> permissions);

    List<Major> listMajors(List<String> roles, List<String> permissions);
}
