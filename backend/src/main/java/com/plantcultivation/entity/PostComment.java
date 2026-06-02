package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post_comment")
public class PostComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private String userAccount;
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String avatarUrl;
}
