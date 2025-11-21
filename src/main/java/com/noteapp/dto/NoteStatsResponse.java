package com.noteapp.dto;

import java.util.List;

public record NoteStatsResponse(
        String noteId,
        List<NoteStatsEntry> stats
) {
}
