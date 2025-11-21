package com.noteapp.service;

import com.mongodb.MongoWriteException;
import com.noteapp.dto.CreateNoteRequest;
import com.noteapp.dto.CreateNoteResponse;
import com.noteapp.mapper.NoteMapper;
import com.noteapp.model.Note;
import com.noteapp.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    public CreateNoteResponse createNote(CreateNoteRequest dto) {
        Note note = new Note();
        note.setTitle(dto.title());
        note.setText(dto.text());
        note.setTags(dto.tags());

        Note savedNote = saveNote(note);

        return noteMapper.toCreateNoteResponse(savedNote);
    }


    private Note saveNote(Note note) {
        try {
            return noteRepository.save(note);
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException("Note saving error", e);
        }
    }
}
