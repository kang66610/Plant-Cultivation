package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("plant_diary")
public class PlantDiary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userAccount;
    private String plantSlug;
    private String plantName;
    private String title;
    private String content;
    private String images;
    private String weather;
    private String mood;
    private Integer heightCm;
    private Integer leafCount;
    private String growthStage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String avatarUrl;
}
