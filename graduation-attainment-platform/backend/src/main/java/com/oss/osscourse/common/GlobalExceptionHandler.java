package com.oss.osscourse.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(400, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return Result.error(400, msg);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException: {}", e.getMessage());
        return Result.error(400, "当前数据已被其他业务数据引用，无法删除或修改");
    }

    @ExceptionHandler(MyBatisSystemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMyBatisSystemException(MyBatisSystemException e) {
        log.warn("MyBatisSystemException: {}", e.getMessage());
        if (hasConnectionFailureCause(e)) {
            return Result.error(500, "数据库连接失败，请联系管理员检查数据库服务、库名和账号配置");
        }
        return Result.error(400, "数据库操作失败，请检查请求数据或稍后重试");
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleCannotGetJdbcConnection(CannotGetJdbcConnectionException e) {
        log.error("CannotGetJdbcConnectionException", e);
        return Result.error(500, "数据库连接失败，请联系管理员检查数据库服务、库名和账号配置");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.warn("RuntimeException: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.error(500, "服务器内部错误");
    }

    private boolean hasConnectionFailureCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof CannotGetJdbcConnectionException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && (
                    message.contains("CannotGetJdbcConnectionException")
                            || message.contains("Communications link failure")
                            || message.contains("Access denied for user")
                            || message.contains("Unknown database")
                            || message.contains("Connection refused"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
