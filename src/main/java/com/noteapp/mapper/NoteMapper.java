package com.noteapp.mapper;

import com.noteapp.dto.NoteDto;
import com.noteapp.model.Note;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoteMapper {
    NoteDto toDto(Note note);
}
