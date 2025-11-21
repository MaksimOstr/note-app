package com.noteapp.mapper;

import com.noteapp.dto.CreateNoteResponse;
import com.noteapp.model.Note;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoteMapper {
    CreateNoteResponse toCreateNoteResponse(Note note);
}
