package com.plantcultivation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank(message = "帖子内容不能为空")
        @Size(max = 5000, message = "帖子内容不能超过5000位")
        String content,

        @Size(max = 5000, message = "图片数据过长")
        String images,

        @Pattern(regexp = "^$|^[a-z0-9-]{1,120}$", message = "植物标识格式不正确")
        String plantSlug,

        @Pattern(regexp = "^$|^[a-z0-9-]{1,60}$", message = "分类标识格式不正确")
        String categorySlug
) {
}
