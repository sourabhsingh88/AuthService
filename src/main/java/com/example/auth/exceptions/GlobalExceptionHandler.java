package com.example.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ===================== 400 BAD REQUEST ===================== */

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return build(HttpStatus.BAD_REQUEST, message);
    }

    /* ===================== 404 NOT FOUND ===================== */

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /* ===================== 401 UNAUTHORIZED ===================== */

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /* ===================== 500 INTERNAL ERROR ===================== */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    /* ===================== RESPONSE BUILDER ===================== */

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}


//package com.example.auth.exceptions;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(BadRequestException.class)
//    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
//        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
//    }
//
//    @ExceptionHandler(NotFoundException.class)
//    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
//        return build(HttpStatus.NOT_FOUND, ex.getMessage());
//    }
//
//    @ExceptionHandler(UnauthorizedException.class)
//    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
//        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleGeneric(Exception ex) {
//        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
//    }
//
//    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("timestamp", LocalDateTime.now());
//        body.put("status", status.value());
//        body.put("error", status.getReasonPhrase());
//        body.put("message", message);
//        return new ResponseEntity<>(body, status);
//    }
//}
