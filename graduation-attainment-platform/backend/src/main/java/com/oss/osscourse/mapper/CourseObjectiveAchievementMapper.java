package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.entity.CourseObjectiveAchievement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseObjectiveAchievementMapper extends BaseMapper<CourseObjectiveAchievement> {

    @Insert({
            "<script>",
            "INSERT INTO course_objective_achievement (class_id, co_id, average_achievement)",
            "VALUES",
            "<foreach collection='items' item='item' separator=','>",
            "(#{item.classId}, #{item.coId}, #{item.averageAchievement})",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE average_achievement = VALUES(average_achievement)",
            "</script>"
    })
    void upsertBatch(@Param("items") List<CourseObjectiveAchievement> items);
}
