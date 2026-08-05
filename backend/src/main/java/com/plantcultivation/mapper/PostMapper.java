package com.plantcultivation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantcultivation.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 原子增减计数（并发安全，避免 read-modify-write 漂移）。
     * GREATEST 保证计数不会因并发重复取消而降到负数。
     * column 仅允许调用方传入白名单值：like_count / comment_count。
     */
    @Update("UPDATE post SET like_count = GREATEST(like_count + #{delta}, 0) WHERE id = #{postId}")
    int incrementLikeCount(@Param("postId") Long postId, @Param("delta") int delta);

    @Update("UPDATE post SET comment_count = GREATEST(comment_count + #{delta}, 0) WHERE id = #{postId}")
    int incrementCommentCount(@Param("postId") Long postId, @Param("delta") int delta);
}
