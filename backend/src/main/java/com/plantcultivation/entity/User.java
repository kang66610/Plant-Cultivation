package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String account;

    @JsonIgnore
    private String password;

    private String avatarUrl;
    private String bio;

    /** 令牌版本：登出/改密时自增，使此前签发的 JWT 立即失效。 */
    @JsonIgnore
    private Integer tokenVersion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
