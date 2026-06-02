package com.plantcultivation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantcultivation.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("SELECT p.*, u.username, u.avatar_url as avatar_url FROM post p " +
            "LEFT JOIN user u ON p.user_account = u.account " +
            "WHERE p.plant_slug = #{plantSlug} " +
            "ORDER BY p.created_at DESC " +
            "LIMIT #{offset}, #{size}")
    List<Post> selectByPlantSlug(@Param("plantSlug") String plantSlug,
                                  @Param("offset") int offset,
                                  @Param("size") int size);
}
