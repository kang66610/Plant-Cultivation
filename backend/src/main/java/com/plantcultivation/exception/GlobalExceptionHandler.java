package com.plantcultivation.exception;

import com.plantcultivation.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResultVO<Void>> handleBusiness(BusinessException e) {
        // 业务异常：状态码与消息直接透传（面向用户，无内部细节）
        return ResponseEntity.status(e.getStatus())
                .body(ResultVO.error(e.getStatus(), e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultVO<Void> handleNotFound(ResourceNotFoundException e) {
        return ResultVO.error(404, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultVO<Void> handleNoResource(NoResourceFoundException e) {
        // Spring 6：静态资源/接口路径不存在时抛出，统一返回 404 而非 500
        return ResultVO.error(404, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultVO<Void> handleGeneral(Exception e) {
        // 未预期异常：完整信息只进日志，不泄露给客户端
        log.error("Unhandled exception", e);
        return ResultVO.error(500, "服务器内部错误");
    }
}
