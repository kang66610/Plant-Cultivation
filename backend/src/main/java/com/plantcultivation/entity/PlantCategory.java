package com.plantcultivation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("plant_category")
public class PlantCategory {
    private Long plantId;
    private Long categoryId;
}
