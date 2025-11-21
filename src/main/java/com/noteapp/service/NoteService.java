package com.noteapp.service;

import com.noteapp.dto.CreateNoteRequest;
import com.noteapp.dto.NoteDto;
import com.noteapp.dto.NoteStatsEntry;
import com.noteapp.dto.NoteStatsResponse;
import com.noteapp.dto.NoteTextResponse;
import com.noteapp.dto.UpdateNoteRequest;
import com.noteapp.exception.NotFoundException;
import com.noteapp.mapper.NoteMapper;
import com.noteapp.model.Note;
import com.noteapp.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    public NoteDto createNote(CreateNoteRequest dto) {
        Note note = new Note();
        note.setTitle(dto.title());
        note.setText(dto.text());
        note.setTags(dto.tags());
        note.setCreatedAt(Instant.now());

        Note savedNote = saveNote(note);

        return noteMapper.toDto(savedNote);
    }


    public NoteTextResponse getText(String id) {
        Note note = findById(id);

        return new NoteTextResponse(note.getText());
    }

    public NoteDto updateNote(UpdateNoteRequest dto, String id) {
        Note note = findById(id);

        Optional.ofNullable(dto.title()).ifPresent(note::setTitle);
        Optional.ofNullable(dto.text()).ifPresent(note::setText);
        Optional.ofNullable(dto.tags()).ifPresent(note::setTags);

        Note updatedNote = saveNote(note);

        return noteMapper.toDto(updatedNote);
    }

    public void deleteNote(String id) {
        Note note = findById(id);

        noteRepository.delete(note);
    }

    public NoteDto getNoteDtoById(String id) {
        Note note = findById(id);
        return noteMapper.toDto(note);
    }

    public NoteStatsResponse getStats(String id) {
        Note note = findById(id);
        List<NoteStatsEntry> entries = calculateStats(note.getText());

        return new NoteStatsResponse(id, entries);
    }


    private Note findById(String id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Note not found"));
    }


    private Note saveNote(Note note) {
        try {
            return noteRepository.save(note);
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException("Note saving error", e);
        }
    }

    private List<NoteStatsEntry> calculateStats(String text) {
        if (!StringUtils.hasText(text)) return List.of();

        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("\\W+"))
                .filter(StringUtils::hasText)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(e -> new NoteStatsEntry(e.getKey(), e.getValue()))
                .toList();
    }
}
