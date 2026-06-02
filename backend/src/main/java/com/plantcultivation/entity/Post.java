package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userAccount;
    private String content;
    private String images;
    private String plantSlug;
    private String categorySlug;
    private Integer likeCount;
    private Integer commentCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String avatarUrl;

    @TableField(exist = false)
    private Boolean liked;

    @TableField(exist = false)
    private String plantName;
}
