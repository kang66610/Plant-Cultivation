package com.plantcultivation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantcultivation.entity.Plant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PlantMapper extends BaseMapper<Plant> {

    @Select("SELECT p.* FROM plant p " +
            "INNER JOIN plant_category pc ON p.id = pc.plant_id " +
            "WHERE pc.category_id = #{categoryId} " +
            "ORDER BY p.common_name")
    List<Plant> selectByCategoryId(@Param("categoryId") Long categoryId);

    @Select("SELECT * FROM plant WHERE is_featured = TRUE ORDER BY view_count DESC LIMIT #{limit}")
    List<Plant> selectFeatured(@Param("limit") int limit);

    @Select("SELECT * FROM plant WHERE MATCH(common_name, scientific_name, description) AGAINST(#{keyword} IN BOOLEAN MODE) LIMIT #{limit}")
    List<Plant> fullTextSearch(@Param("keyword") String keyword, @Param("limit") int limit);
}
