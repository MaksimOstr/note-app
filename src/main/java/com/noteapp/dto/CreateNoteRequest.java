package com.noteapp.dto;

import com.noteapp.model.NoteTagEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateNoteRequest(
        @NotBlank
        String title,

        @NotBlank
        String text,

        @NotNull
        List<NoteTagEnum> tags
) {
}
