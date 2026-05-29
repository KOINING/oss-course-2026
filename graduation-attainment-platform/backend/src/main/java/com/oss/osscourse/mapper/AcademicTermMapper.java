package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.entity.AcademicTerm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AcademicTermMapper extends BaseMapper<AcademicTerm> {
    @Select("SELECT COUNT(1) FROM teaching_class WHERE term_id = #{termId}")
    Long countTeachingClassReferences(@Param("termId") Long termId);

    @Select("SELECT COUNT(1) FROM major_indicator_achievement WHERE term_id = #{termId}")
    Long countMajorIndicatorAchievementReferences(@Param("termId") Long termId);
}
