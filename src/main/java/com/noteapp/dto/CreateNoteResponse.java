package com.noteapp.dto;

import java.time.Instant;

public record CreateNoteResponse(
        String id,
        String title,
        String text,
        Instant createdAt
) {
}
