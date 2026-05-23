package com.oss.osscourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oss.osscourse.dto.requirement.IndicatorPointResponse;
import com.oss.osscourse.entity.IndicatorPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IndicatorPointMapper extends BaseMapper<IndicatorPoint> {

    @Select({
            "<script>",
            "SELECT ip.ip_id AS ipId, ip.ip_code AS ipCode, ip.ip_description AS ipDescription,",
            "ip.gr_id AS grId, gr.gr_code AS grCode, gr.gr_description AS grDescription",
            "FROM indicator_point ip",
            "LEFT JOIN graduation_requirement gr ON ip.gr_id = gr.gr_id",
            "<where>",
            "<if test='ipCode != null and ipCode != \"\"'>",
            "AND ip.ip_code LIKE CONCAT('%', #{ipCode}, '%')",
            "</if>",
            "<if test='grId != null'>",
            "AND ip.gr_id = #{grId}",
            "</if>",
            "</where>",
            "ORDER BY ip.gr_id ASC, ip.ip_code ASC",
            "</script>"
    })
    List<IndicatorPointResponse> selectIndicatorPointList(@Param("ipCode") String ipCode,
                                                            @Param("grId") Long grId);

    @Select("SELECT COUNT(*) FROM indicator_point WHERE gr_id = #{grId}")
    int countByGrId(@Param("grId") Long grId);
}
