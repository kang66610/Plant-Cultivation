package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_plant_collection")
public class UserPlantCollection {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long plantId;

    private String nickname;
    private String location;

    /** 浇水间隔天数（收藏时从植物养护参数取默认值，可后续调整） */
    private Integer waterIntervalDays;

    private LocalDateTime lastWateredAt;
    private LocalDateTime nextWaterAt;
    private String notes;
    private LocalDateTime createdAt;

    // ---- 列表展示冗余字段（查询时按 plant_id 批量组装，非表字段） ----
    @TableField(exist = false)
    private String plantName;
    @TableField(exist = false)
    private String plantImage;
    @TableField(exist = false)
    private String plantSlug;
}
