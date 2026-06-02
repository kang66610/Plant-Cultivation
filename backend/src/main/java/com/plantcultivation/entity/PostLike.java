package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post_like")
public class PostLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private String userAccount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
