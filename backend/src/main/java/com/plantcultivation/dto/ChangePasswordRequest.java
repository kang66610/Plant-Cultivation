package com.plantcultivation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "原密码不能为空")
        @Size(max = 100, message = "原密码长度不能超过100位")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 100, message = "新密码长度需为6-100位")
        String newPassword
) {
}
