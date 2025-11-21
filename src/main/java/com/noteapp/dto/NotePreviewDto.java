package com.noteapp.dto;

import java.time.Instant;

public record NotePreviewDto(
        String id,
        String title,
        Instant createdDate
) {
}
