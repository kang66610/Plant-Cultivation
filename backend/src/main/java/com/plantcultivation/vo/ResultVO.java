package com.plantcultivation.vo;

import lombok.Data;

@Data
public class ResultVO<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> ResultVO<T> success() {
        return success(null);
    }

    public static <T> ResultVO<T> error(int code, String message) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
