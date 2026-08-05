package com.plantcultivation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "账号不能为空")
        @Size(max = 50, message = "账号长度不能超过50位")
        String account,

        @NotBlank(message = "密码不能为空")
        @Size(max = 100, message = "密码长度不能超过100位")
        String password
) {
}
