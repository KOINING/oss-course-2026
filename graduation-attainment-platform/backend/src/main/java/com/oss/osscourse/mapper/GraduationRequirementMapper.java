package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.dto.requirement.GraduationRequirementResponse;
import com.oss.osscourse.entity.GraduationRequirement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GraduationRequirementMapper extends BaseMapper<GraduationRequirement> {

    @Select({
            "<script>",
            "SELECT gr.gr_id AS grId, gr.gr_code AS grCode, gr.gr_description AS grDescription,",
            "gr.major_id AS majorId, m.major_name AS majorName",
            "FROM GraduationRequirement gr",
            "LEFT JOIN Major m ON gr.major_id = m.major_id",
            "<where>",
            "<if test='grCode != null and grCode != \"\"'>",
            "AND gr.gr_code LIKE CONCAT('%', #{grCode}, '%')",
            "</if>",
            "<if test='majorId != null'>",
            "AND gr.major_id = #{majorId}",
            "</if>",
            "</where>",
            "ORDER BY gr.gr_id ASC",
            "</script>"
    })
    List<GraduationRequirementResponse> selectRequirementList(@Param("grCode") String grCode,
                                                               @Param("majorId") Long majorId);
}
