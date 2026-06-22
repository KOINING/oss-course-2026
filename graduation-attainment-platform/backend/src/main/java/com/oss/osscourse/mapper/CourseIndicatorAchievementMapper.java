package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.entity.CourseIndicatorAchievement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseIndicatorAchievementMapper extends BaseMapper<CourseIndicatorAchievement> {

    @Insert({
            "<script>",
            "INSERT INTO course_indicator_achievement (class_id, ip_id, achievement, is_locked)",
            "VALUES",
            "<foreach collection='items' item='item' separator=','>",
            "(#{item.classId}, #{item.ipId}, #{item.achievement}, #{item.isLocked})",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE achievement = VALUES(achievement), is_locked = VALUES(is_locked)",
            "</script>"
    })
    void upsertBatch(@Param("items") List<CourseIndicatorAchievement> items);
}
