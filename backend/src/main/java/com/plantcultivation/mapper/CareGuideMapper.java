package com.plantcultivation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantcultivation.entity.CareGuide;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CareGuideMapper extends BaseMapper<CareGuide> {

    @Select("SELECT * FROM care_guide WHERE plant_id = #{plantId} ORDER BY display_order")
    List<CareGuide> selectByPlantId(@Param("plantId") Long plantId);

    @Select("SELECT * FROM care_guide WHERE plant_id = #{plantId} AND season = #{season} ORDER BY display_order")
    List<CareGuide> selectByPlantIdAndSeason(@Param("plantId") Long plantId, @Param("season") String season);
}
