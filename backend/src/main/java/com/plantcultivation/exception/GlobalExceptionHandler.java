package com.plantcultivation.exception;

import com.plantcultivation.vo.ResultVO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultVO<Void> handleNotFound(ResourceNotFoundException e) {
        return ResultVO.error(404, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultVO<Void> handleGeneral(Exception e) {
        return ResultVO.error(500, "Internal server error: " + e.getMessage());
    }
}
