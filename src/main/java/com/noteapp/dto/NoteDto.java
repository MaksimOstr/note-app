package com.noteapp.dto;

import com.noteapp.model.NoteTagEnum;

import java.time.Instant;
import java.util.List;

public record NoteDto(
        String id,
        String title,
        String text,
        List<NoteTagEnum> tags,
        Instant createdAt
) {
}
