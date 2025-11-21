package com.noteapp.service;

import com.noteapp.dto.CreateNoteRequest;
import com.noteapp.dto.NoteDto;
import com.noteapp.dto.NoteParams;
import com.noteapp.dto.NotePreviewDto;
import com.noteapp.dto.NoteStatsEntry;
import com.noteapp.dto.NoteStatsResponse;
import com.noteapp.dto.NoteTextResponse;
import com.noteapp.dto.UpdateNoteRequest;
import com.noteapp.exception.NotFoundException;
import com.noteapp.mapper.NoteMapper;
import com.noteapp.model.Note;
import com.noteapp.model.NoteTagEnum;
import com.noteapp.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteService noteService;

    @Test
    void createNote_persistsAndReturnsDto() {
        List<NoteTagEnum> tags = List.of(NoteTagEnum.BUSINESS, NoteTagEnum.IMPORTANT);
        CreateNoteRequest request = new CreateNoteRequest("Daily note", "Focus on delivery", tags);
        Note savedNote = noteWithId("note-id");
        NoteDto dto = new NoteDto(savedNote.getId(), savedNote.getTitle(), savedNote.getText(), savedNote.getTags(), savedNote.getCreatedDate());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
        when(noteMapper.toDto(savedNote)).thenReturn(dto);

        NoteDto result = noteService.createNote(request);

        assertThat(result).isEqualTo(dto);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note persisted = captor.getValue();
        assertThat(persisted.getTitle()).isEqualTo(request.title());
        assertThat(persisted.getText()).isEqualTo(request.text());
        assertThat(persisted.getTags()).containsExactlyElementsOf(tags);
        assertThat(persisted.getCreatedDate()).isNotNull();
        verify(noteMapper).toDto(savedNote);
    }

    @Test
    void createNote_wrapsDataIntegrityViolations() {
        CreateNoteRequest request = new CreateNoteRequest("Duplicate", "Duplicated body", List.of(NoteTagEnum.PERSONAL));
        when(noteRepository.save(any(Note.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> noteService.createNote(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Note saving error");
    }

    @Test
    void getText_returnsNoteTextResponse() {
        Note note = noteWithId("text-id");
        NoteTextResponse response = new NoteTextResponse("text-id", "body");
        when(noteRepository.findById("text-id")).thenReturn(Optional.of(note));
        when(noteMapper.toNoteTextResponse(note)).thenReturn(response);

        NoteTextResponse result = noteService.getText("text-id");

        assertThat(result).isEqualTo(response);
        verify(noteRepository).findById("text-id");
    }

    @Test
    void getNoteDtoById_returnsMappedDto() {
        Note note = noteWithId("dto-id");
        NoteDto dto = new NoteDto("dto-id", "title", "text", note.getTags(), note.getCreatedDate());
        when(noteRepository.findById("dto-id")).thenReturn(Optional.of(note));
        when(noteMapper.toDto(note)).thenReturn(dto);

        NoteDto result = noteService.getNoteDtoById("dto-id");

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getNoteDtoById_throwsWhenNotFound() {
        when(noteRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getNoteDtoById("missing"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateNote_updatesProvidedFields() {
        Note note = noteWithId("update-id");
        note.setTitle("old title");
        note.setText("old text");
        note.setTags(new ArrayList<>(List.of(NoteTagEnum.PERSONAL)));
        UpdateNoteRequest request = new UpdateNoteRequest("new title", "new text", List.of(NoteTagEnum.BUSINESS));
        NoteDto dto = new NoteDto("update-id", "new title", "new text", request.tags(), note.getCreatedDate());
        when(noteRepository.findById("update-id")).thenReturn(Optional.of(note));
        when(noteRepository.save(note)).thenReturn(note);
        when(noteMapper.toDto(note)).thenReturn(dto);

        NoteDto result = noteService.updateNote(request, "update-id");

        assertThat(result).isEqualTo(dto);
        assertThat(note.getTitle()).isEqualTo("new title");
        assertThat(note.getText()).isEqualTo("new text");
        assertThat(note.getTags()).containsExactlyElementsOf(request.tags());
    }

    @Test
    void updateNote_keepsExistingValuesWhenNulls() {
        Note note = noteWithId("partial-id");
        note.setTitle("stable");
        note.setText("content");
        note.setTags(new ArrayList<>(List.of(NoteTagEnum.IMPORTANT)));
        UpdateNoteRequest request = new UpdateNoteRequest(null, null, null);
        NoteDto dto = new NoteDto("partial-id", "stable", "content", note.getTags(), note.getCreatedDate());
        when(noteRepository.findById("partial-id")).thenReturn(Optional.of(note));
        when(noteRepository.save(note)).thenReturn(note);
        when(noteMapper.toDto(note)).thenReturn(dto);

        noteService.updateNote(request, "partial-id");

        assertThat(note.getTitle()).isEqualTo("stable");
        assertThat(note.getText()).isEqualTo("content");
        assertThat(note.getTags()).containsExactlyElementsOf(List.of(NoteTagEnum.IMPORTANT));
    }

    @Test
    void deleteNote_removesRetrievedEntity() {
        Note note = noteWithId("delete-id");
        when(noteRepository.findById("delete-id")).thenReturn(Optional.of(note));

        noteService.deleteNote("delete-id");

        verify(noteRepository).delete(note);
    }

    @Test
    void getStats_returnsOrderedWordOccurrences() {
        Note note = noteWithId("stats-id");
        note.setText("Note is just a note, NOTE!");
        when(noteRepository.findById("stats-id")).thenReturn(Optional.of(note));

        NoteStatsResponse response = noteService.getStats("stats-id");

        assertThat(response.noteId()).isEqualTo("stats-id");
        assertThat(response.stats()).containsExactly(
                new NoteStatsEntry("note", 3),
                new NoteStatsEntry("a", 1),
                new NoteStatsEntry("is", 1),
                new NoteStatsEntry("just", 1)
        );
    }

    @Test
    void getStats_returnsEmptyWhenNoText() {
        Note note = noteWithId("empty-stats");
        note.setText("");
        when(noteRepository.findById("empty-stats")).thenReturn(Optional.of(note));

        NoteStatsResponse response = noteService.getStats("empty-stats");

        assertThat(response.stats()).isEmpty();
    }

    @Test
    void getNotePreviews_returnsMappedPage() {
        List<NoteTagEnum> tags = List.of(NoteTagEnum.BUSINESS);
        NoteParams params = new NoteParams(tags);
        Pageable pageable = PageRequest.of(1, 5);
        Note note = noteWithId("preview-id");
        Page<Note> notePage = new PageImpl<>(List.of(note), pageable, 12);
        List<NotePreviewDto> previews = List.of(new NotePreviewDto("preview-id", "Daily", note.getCreatedDate()));

        when(noteRepository.findByTagsIn(tags, pageable)).thenReturn(notePage);
        when(noteMapper.toPreviewDtoList(notePage)).thenReturn(previews);

        Page<NotePreviewDto> result = noteService.getNotePreviews(params, pageable);

        assertThat(result.getContent()).isEqualTo(previews);
        assertThat(result.getTotalElements()).isEqualTo(12);
        assertThat(result.getPageable()).isEqualTo(pageable);
        verify(noteRepository).findByTagsIn(tags, pageable);
    }

    private Note noteWithId(String id) {
        Note note = new Note();
        note.setId(id);
        note.setTitle("Sample");
        note.setText("Sample text");
        note.setTags(new ArrayList<>(List.of(NoteTagEnum.PERSONAL)));
        note.setCreatedDate(Instant.parse("2024-01-01T00:00:00Z"));
        return note;
    }
}
