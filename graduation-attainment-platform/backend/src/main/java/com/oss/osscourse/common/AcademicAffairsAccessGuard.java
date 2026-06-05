package com.oss.osscourse.common;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AcademicAffairsAccessGuard {

    private static final String REQUIRED_ROLE = "academic_affairs";

    public void assertAcademicAffairs(List<String> roles) {
        if (roles == null || !roles.contains(REQUIRED_ROLE)) {
            throw new BusinessException(403, "仅教务管理人员可访问该功能");
        }
    }
}
