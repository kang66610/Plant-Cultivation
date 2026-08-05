package com.plantcultivation.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(max = 50, message = "用户名长度不能超过50位")
        String username,

        @Size(max = 300, message = "简介长度不能超过300位")
        String bio,

        @Size(max = 500, message = "头像地址长度不能超过500位")
        String avatarUrl
) {
}
