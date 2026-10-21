package com.plantcultivation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantcultivation.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 按主键批量查询用户（社区/日记列表一次性组装作者信息，避免 N+1）。
     */
    @Select("<script>" +
            "SELECT id, username, account, avatar_url, bio, created_at, updated_at FROM user WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<User> selectUsersByIds(@Param("ids") Collection<Long> ids);

    /**
     * 令牌版本自增：登出或改密后调用，使此前签发的 JWT 全部失效。
     */
    @Update("UPDATE user SET token_version = token_version + 1 WHERE account = #{account}")
    int incrementTokenVersion(@Param("account") String account);
}
