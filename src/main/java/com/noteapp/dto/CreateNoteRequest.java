package com.noteapp.dto;

import com.noteapp.model.NoteTagEnum;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateNoteRequest(
        @NotBlank
        String title,

        @NotBlank
        String text,

        List<NoteTagEnum> tags
) {
}
