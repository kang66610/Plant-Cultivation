package com.plantcultivation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateCollectionRequest(
        @Size(max = 50, message = "昵称长度不能超过50位")
        String nickname,

        @Size(max = 100, message = "摆放位置长度不能超过100位")
        String location,

        @Min(value = 1, message = "浇水周期不能小于1天")
        @Max(value = 365, message = "浇水周期不能超过365天")
        Integer waterIntervalDays,

        @Size(max = 1000, message = "备注不能超过1000位")
        String notes
) {
}
