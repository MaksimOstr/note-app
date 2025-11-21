package com.noteapp.dto;

import com.noteapp.model.NoteTagEnum;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateNoteRequest(
        @Size(min = 1)
        String title,

        @Size(min = 1)
        String text,

        List<NoteTagEnum> tags
) {
}
