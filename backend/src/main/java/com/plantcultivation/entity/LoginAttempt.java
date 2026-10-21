package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 一次登录/注册失败记录。
 * <p>持久化到 MySQL 而不是放在内存 Map 里：多实例部署、进程重启后限流状态依然有效。</p>
 */
@Data
@TableName("login_attempt")
public class LoginAttempt {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 限流维度：account:xxx 或 ip:x.x.x.x */
    private String rateKey;

    private LocalDateTime attemptedAt;
}
