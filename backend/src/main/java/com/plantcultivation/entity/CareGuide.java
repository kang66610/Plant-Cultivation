package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("care_guide")
public class CareGuide {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long plantId;
    private String season;
    private String careType;
    private String title;
    private String content;
    private String tips;          // JSON string
    private String commonMistakes; // JSON string
    private Integer displayOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
