package com.plantcultivation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantcultivation.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("<script>" +
            "SELECT * FROM user WHERE account IN " +
            "<foreach item='acc' collection='accounts' open='(' separator=',' close=')'>" +
            "#{acc}" +
            "</foreach>" +
            "</script>")
    List<User> selectByAccounts(@Param("accounts") List<String> accounts);
}
