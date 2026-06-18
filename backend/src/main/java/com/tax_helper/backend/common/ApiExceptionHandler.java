package com.tax_helper.backend.common;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException exception) {
        log.debug("Business validation failed: {}", exception.getMessage());
        return Map.of(
                "code", "BAD_REQUEST",
                "message", "요청 내용을 확인해 주세요."
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException exception) {
        log.debug("Request validation failed", exception);
        return Map.of(
                "code", "VALIDATION_ERROR",
                "message", "필수 입력값 또는 형식이 올바르지 않습니다."
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleUnexpected(Exception exception) {
        log.error("Unexpected API error", exception);
        return Map.of(
                "code", "INTERNAL_ERROR",
                "message", "처리 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."
        );
    }
}
