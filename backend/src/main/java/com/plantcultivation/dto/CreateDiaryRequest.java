package com.plantcultivation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDiaryRequest(
        @NotBlank(message = "日记标题不能为空")
        @Size(max = 150, message = "日记标题不能超过150位")
        String title,

        @Size(max = 5000, message = "日记内容不能超过5000位")
        String content,

        @Pattern(regexp = "^$|^[a-z0-9-]{1,120}$", message = "植物标识格式不正确")
        String plantSlug,

        @Size(max = 100, message = "植物名称不能超过100位")
        String plantName,

        @Size(max = 5000, message = "图片数据过长")
        String images,

        @Size(max = 50, message = "天气长度不能超过50位")
        String weather,

        @Size(max = 50, message = "心情长度不能超过50位")
        String mood,

        @Min(value = 0, message = "株高不能小于0")
        @Max(value = 999, message = "株高不能超过999cm")
        Integer heightCm,

        @Min(value = 0, message = "叶片数不能小于0")
        @Max(value = 9999, message = "叶片数不能超过9999")
        Integer leafCount,

        @Size(max = 30, message = "生长阶段长度不能超过30位")
        String growthStage
) {
}
