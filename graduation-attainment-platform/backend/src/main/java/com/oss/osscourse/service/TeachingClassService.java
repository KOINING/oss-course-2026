package com.oss.osscourse.service;

import com.oss.osscourse.entity.TeachingClass;

import java.util.List;

public interface TeachingClassService {

    TeachingClass getByCode(String teachingClassCode);

    List<TeachingClass> listAll();
}
