package com.plantcultivation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantcultivation.entity.LoginAttempt;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface LoginAttemptMapper extends BaseMapper<LoginAttempt> {

    /** 统计窗口内的失败次数。 */
    @Select("SELECT COUNT(*) FROM login_attempt " +
            "WHERE rate_key = #{rateKey} AND attempted_at >= #{since}")
    int countSince(@Param("rateKey") String rateKey, @Param("since") LocalDateTime since);

    /** 登录成功后清零该维度的失败记录。 */
    @Delete("DELETE FROM login_attempt WHERE rate_key = #{rateKey}")
    int deleteByKey(@Param("rateKey") String rateKey);

    /** 清理过期记录，防止表无限增长（由定时任务调用）。 */
    @Delete("DELETE FROM login_attempt WHERE attempted_at < #{before}")
    int deleteOlderThan(@Param("before") LocalDateTime before);
}
