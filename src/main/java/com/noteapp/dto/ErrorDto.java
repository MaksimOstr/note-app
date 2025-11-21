package com.noteapp.dto;

public record ErrorDto(
        String code,

        int status,

        String message
) {
}
