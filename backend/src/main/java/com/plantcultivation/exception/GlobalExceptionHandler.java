package com.plantcultivation.exception;

import com.plantcultivation.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResultVO<Void>> handleMaxUpload(MaxUploadSizeExceededException e) {
        // 超过 multipart 上限（application.yml max-file-size: 10MB）时 Spring 抛此异常，
        // 语义是客户端错误（413），不能落到兜底 500
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ResultVO.error(413, "文件大小不能超过10MB"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultVO<Void>> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return ResponseEntity.badRequest().body(ResultVO.error(400, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResultVO<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("参数校验失败");
        return ResponseEntity.badRequest().body(ResultVO.error(400, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResultVO<Void>> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(ResultVO.error(400, "请求体格式不正确"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResultVO<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(ResultVO.error(400, "缺少必要参数：" + e.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResultVO<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(ResultVO.error(400, "参数类型不正确：" + e.getName()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResultVO<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ResultVO.error(405, "请求方法不支持"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResultVO<Void>> handleAuthentication(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResultVO.error(401, "请先登录"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResultVO<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultVO.error(403, "无权限访问"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ResultVO<Void>> handleMultipart(MultipartException e) {
        return ResponseEntity.badRequest().body(ResultVO.error(400, "上传文件格式不正确"));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultVO<Void> handleGeneral(Exception e) {
        // 未预期异常：完整信息只进日志，不泄露给客户端
        log.error("Unhandled exception", e);
        return ResultVO.error(500, "服务器内部错误");
    }
}
