package com.plantcultivation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50位")
        String username,

        @NotBlank(message = "账号不能为空")
        @Size(min = 3, max = 50, message = "账号长度需为3-50位")
        @Pattern(regexp = "^[A-Za-z0-9_@.-]+$", message = "账号只能包含字母、数字、下划线、点、@或短横线")
        String account,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度需为6-100位")
        String password
) {
}
