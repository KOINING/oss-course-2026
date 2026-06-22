package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.entity.StudentObjectiveAchievement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentObjectiveAchievementMapper extends BaseMapper<StudentObjectiveAchievement> {

    @Insert({
            "<script>",
            "INSERT INTO student_objective_achievement (student_id, class_id, co_id, achievement)",
            "VALUES",
            "<foreach collection='items' item='item' separator=','>",
            "(#{item.studentId}, #{item.classId}, #{item.coId}, #{item.achievement})",
            "</foreach>",
            "ON DUPLICATE KEY UPDATE achievement = VALUES(achievement)",
            "</script>"
    })
    void upsertBatch(@Param("items") List<StudentObjectiveAchievement> items);
}
