package com.noteapp.mapper;

import com.noteapp.dto.NoteDto;
import com.noteapp.dto.NotePreviewDto;
import com.noteapp.dto.NoteTextResponse;
import com.noteapp.model.Note;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NoteMapper {
    NoteTextResponse toNoteTextResponse(Note note);

    NoteDto toDto(Note note);

    NotePreviewDto toPreviewDto(Note note);

    List<NotePreviewDto> toPreviewDtoList(Page<Note> notes);
}
