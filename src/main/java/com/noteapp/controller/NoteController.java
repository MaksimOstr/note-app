package com.noteapp.controller;

import com.noteapp.dto.CreateNoteRequest;
import com.noteapp.dto.NoteDto;
import com.noteapp.dto.NoteStatsResponse;
import com.noteapp.dto.NoteTextResponse;
import com.noteapp.dto.UpdateNoteRequest;
import com.noteapp.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<NoteDto> createNote(
            @RequestBody @Valid CreateNoteRequest body
    ) {
        NoteDto noteResponse = noteService.createNote(body);

        return ResponseEntity.ok(noteResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> getNote(
            @PathVariable String id
    ) {
        NoteDto note = noteService.getNoteDtoById(id);

        return ResponseEntity.ok(note);
    }

    @GetMapping("/{id}/text")
    public ResponseEntity<NoteTextResponse> getNoteText(
            @PathVariable String id
    ) {
        NoteTextResponse noteTextResponse = noteService.getText(id);

        return ResponseEntity.ok(noteTextResponse);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<NoteStatsResponse> stats(@PathVariable String id) {
        NoteStatsResponse statsResponse = noteService.getStats(id);

        return ResponseEntity.ok(statsResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(
            @PathVariable String id,
            @RequestBody @Valid UpdateNoteRequest body
    ) {
        NoteDto note = noteService.updateNote(body, id);

        return ResponseEntity.ok(note);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<NoteDto> deleteNote(
            @PathVariable String id
    ) {
        noteService.deleteNote(id);

        return ResponseEntity.ok().build();
    }
}
