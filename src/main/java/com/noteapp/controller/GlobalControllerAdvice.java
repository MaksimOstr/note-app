package com.noteapp.controller;

import com.noteapp.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<Map<String, String>> handleNotValidMethodArguments(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorDto(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        ));
    }


}
