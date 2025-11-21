package com.noteapp.dto;

import com.noteapp.model.NoteTagEnum;

import java.util.List;

public record NoteParams(
        List<NoteTagEnum> tags
) {
}
