package com.oss.osscourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oss.osscourse.common.BusinessException;
import com.oss.osscourse.entity.TeachingClass;
import com.oss.osscourse.mapper.TeachingClassMapper;
import com.oss.osscourse.service.TeachingClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeachingClassServiceImpl implements TeachingClassService {

    private final TeachingClassMapper teachingClassMapper;

    @Override
    public TeachingClass getByCode(String teachingClassCode) {
        if (teachingClassCode == null || teachingClassCode.trim().isEmpty()) {
            throw new BusinessException(400, "教学班编号不能为空");
        }

        List<TeachingClass> list = teachingClassMapper.selectList(
                new LambdaQueryWrapper<TeachingClass>()
                        .eq(TeachingClass::getTeachingClassCode, teachingClassCode.trim()));

        if (list.isEmpty()) {
            throw new BusinessException(400, "教学班编号不存在: " + teachingClassCode);
        }
        if (list.size() > 1) {
            throw new BusinessException(400, "教学班编号不唯一: " + teachingClassCode);
        }
        return list.get(0);
    }

    @Override
    public List<TeachingClass> listAll() {
        return teachingClassMapper.selectList(
                new LambdaQueryWrapper<TeachingClass>()
                        .orderByAsc(TeachingClass::getTeachingClassCode));
    }
}
