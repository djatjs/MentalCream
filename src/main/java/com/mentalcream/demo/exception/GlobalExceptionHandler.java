package com.mentalcream.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, org.springframework.web.context.request.WebRequest request) {
        ex.printStackTrace(); // 콘솔에 에러 내용을 출력합니다.
        return new ResponseEntity<>(java.util.Map.of("message", "An unexpected error occurred: " + ex.getMessage()), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
