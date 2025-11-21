package com.noteapp.controller;

import com.noteapp.dto.CreateNoteRequest;
import com.noteapp.dto.CreateNoteResponse;
import com.noteapp.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<CreateNoteResponse> createNote(
            @RequestBody @Valid CreateNoteRequest body
    ) {
        CreateNoteResponse noteResponse = noteService.createNote(body);

        return ResponseEntity.ok(noteResponse);
    }
}
