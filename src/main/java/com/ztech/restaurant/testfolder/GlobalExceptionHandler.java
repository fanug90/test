package com.ztech.restaurant.testfolder;

import java.util.List;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    public record ApiError(String code, String message, String correlationId, List<String> details) {}

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> api(ApiException e, HttpServletRequest r) { return build(e.status(), e.code(), e.getMessage(), r); }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> bad(HttpMessageNotReadableException e, HttpServletRequest r) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request body is invalid.", r);
    }

    private ResponseEntity<ApiError> build(HttpStatus s, String code, String msg, HttpServletRequest r) {
        return ResponseEntity.status(s).body(new ApiError(code, msg, r.getHeader("X-Correlation-ID"), List.of()));
    }
}
