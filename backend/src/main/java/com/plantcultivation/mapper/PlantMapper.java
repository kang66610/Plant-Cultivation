package com.plantcultivation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @Select("""
            <script>
            SELECT p.* FROM plant p
            INNER JOIN plant_category pc ON p.id = pc.plant_id
            INNER JOIN category c ON c.id = pc.category_id
            WHERE c.slug = #{categorySlug}
            <if test="search != null and search != ''">
              AND (p.common_name LIKE CONCAT('%', #{search}, '%')
                   OR p.scientific_name LIKE CONCAT('%', #{search}, '%'))
            </if>
            <if test="light != null and light != ''">
              AND p.light_level = #{light}
            </if>
            <if test="water != null and water != ''">
              AND p.water_frequency = #{water}
            </if>
            <if test="difficulty != null and difficulty != ''">
              AND p.difficulty = #{difficulty}
            </if>
            <if test="indoor != null">
              AND p.is_indoor = #{indoor}
            </if>
            <if test="petSafe != null">
              AND p.is_pet_safe = #{petSafe}
            </if>
            ORDER BY p.common_name
            </script>
            """)
    Page<Plant> selectPageByCategorySlug(Page<Plant> page,
                                         @Param("categorySlug") String categorySlug,
                                         @Param("search") String search,
                                         @Param("light") String light,
                                         @Param("water") String water,
                                         @Param("difficulty") String difficulty,
                                         @Param("indoor") Boolean indoor,
                                         @Param("petSafe") Boolean petSafe);

    @Select("SELECT p.* FROM plant p " +
            "INNER JOIN plant_category pc ON p.id = pc.plant_id " +
            "INNER JOIN category c ON c.id = pc.category_id " +
            "WHERE c.slug = #{categorySlug} " +
            "ORDER BY p.common_name")
    List<Plant> selectByCategorySlug(@Param("categorySlug") String categorySlug);

    @Select("SELECT * FROM plant WHERE is_featured = TRUE ORDER BY view_count DESC LIMIT #{limit}")
    List<Plant> selectFeatured(@Param("limit") int limit);

    @Select("SELECT * FROM plant WHERE MATCH(common_name, scientific_name, description) AGAINST(#{keyword} IN BOOLEAN MODE) LIMIT #{limit}")
    List<Plant> fullTextSearch(@Param("keyword") String keyword, @Param("limit") int limit);

    /** 自然语言模式搜索：用户输入中的特殊字符不会触发 BOOLEAN MODE 语法错误 */
    @Select("SELECT * FROM plant WHERE MATCH(common_name, scientific_name, description) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) LIMIT #{limit}")
    List<Plant> naturalSearch(@Param("keyword") String keyword, @Param("limit") int limit);
}
