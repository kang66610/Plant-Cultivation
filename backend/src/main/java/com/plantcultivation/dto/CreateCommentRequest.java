package com.plantcultivation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 1000, message = "评论内容不能超过1000位")
        String content
) {
}
