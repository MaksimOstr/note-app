package com.noteapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noteapp.dto.CreateNoteRequest;
import com.noteapp.dto.NoteDto;
import com.noteapp.dto.NoteParams;
import com.noteapp.dto.NotePreviewDto;
import com.noteapp.dto.NoteStatsEntry;
import com.noteapp.dto.NoteStatsResponse;
import com.noteapp.dto.NoteTextResponse;
import com.noteapp.dto.UpdateNoteRequest;
import com.noteapp.model.NoteTagEnum;
import com.noteapp.service.NoteService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoteService noteService;

    @TestConfiguration
    static class MongoTestConfig {
        @Bean
        MongoMappingContext mongoMappingContext() {
            return new MongoMappingContext();
        }
    }

    @Test
    void createNote_returnsSavedNote() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest("Sprint plan", "Outline tasks", List.of(NoteTagEnum.BUSINESS));
        Instant created = Instant.parse("2024-05-10T10:15:30Z");
        NoteDto dto = new NoteDto("note-id", request.title(), request.text(), request.tags(), created);
        when(noteService.createNote(any(CreateNoteRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("note-id"))
                .andExpect(jsonPath("$.title").value("Sprint plan"))
                .andExpect(jsonPath("$.createdDate").value(created.toString()))
                .andExpect(jsonPath("$.tags[0]").value("BUSINESS"));
    }

    @Test
    void createNote_returnsBadRequestWhenRequiredFieldsMissing() throws Exception {
        String payload = """
                {
                  "title": "",
                  "text": "",
                  "tags": null
                }
                """;

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("must not be blank"))
                .andExpect(jsonPath("$.text").value("must not be blank"))
                .andExpect(jsonPath("$.tags").value("must not be null"));
    }

    @Test
    void getAllNotes_usesFiltersPaginationAndSort() throws Exception {
        Instant created = Instant.parse("2024-06-15T08:00:00Z");
        NotePreviewDto previewDto = new NotePreviewDto("n1", "Release checklist", created);
        Page<NotePreviewDto> page = new PageImpl<>(List.of(previewDto), PageRequest.of(2, 5), 25);
        when(noteService.getNotePreviews(any(NoteParams.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/notes")
                        .param("tags", "BUSINESS", "PERSONAL")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("n1"))
                .andExpect(jsonPath("$.content[0].title").value("Release checklist"))
                .andExpect(jsonPath("$.content[0].createdDate").value(created.toString()))
                .andExpect(jsonPath("$.content[0].text").doesNotExist());

        ArgumentCaptor<NoteParams> paramsCaptor = ArgumentCaptor.forClass(NoteParams.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(noteService).getNotePreviews(paramsCaptor.capture(), pageableCaptor.capture());

        assertThat(paramsCaptor.getValue().tags()).containsExactly(NoteTagEnum.BUSINESS, NoteTagEnum.PERSONAL);
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        Sort.Order order = pageable.getSort().getOrderFor("createdDate");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getAllNotes_usesDefaultPaginationWhenMissing() throws Exception {
        Page<NotePreviewDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(noteService.getNotePreviews(any(NoteParams.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(noteService).getNotePreviews(any(NoteParams.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void getNote_returnsNoteDto() throws Exception {
        NoteDto dto = new NoteDto("read-id", "Notebook", "Full text", List.of(NoteTagEnum.PERSONAL), Instant.parse("2024-06-01T00:00:00Z"));
        when(noteService.getNoteDtoById("read-id")).thenReturn(dto);

        mockMvc.perform(get("/api/notes/{id}", "read-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("read-id"))
                .andExpect(jsonPath("$.text").value("Full text"))
                .andExpect(jsonPath("$.tags[0]").value("PERSONAL"));

        verify(noteService).getNoteDtoById("read-id");
    }

    @Test
    void getNoteText_returnsTextResponse() throws Exception {
        NoteTextResponse response = new NoteTextResponse("text-id", "Individual text");
        when(noteService.getText("text-id")).thenReturn(response);

        mockMvc.perform(get("/api/notes/{id}/text", "text-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("text-id"))
                .andExpect(jsonPath("$.text").value("Individual text"));

        verify(noteService).getText("text-id");
    }

    @Test
    void getNoteStats_returnsStatsResponse() throws Exception {
        List<NoteStatsEntry> stats = List.of(new NoteStatsEntry("note", 2), new NoteStatsEntry("is", 1));
        NoteStatsResponse response = new NoteStatsResponse("stats-id", stats);
        when(noteService.getStats("stats-id")).thenReturn(response);

        mockMvc.perform(get("/api/notes/{id}/stats", "stats-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteId").value("stats-id"))
                .andExpect(jsonPath("$.stats[0].word").value("note"))
                .andExpect(jsonPath("$.stats[0].count").value(2));

        verify(noteService).getStats("stats-id");
    }

    @Test
    void updateNote_returnsUpdatedDto() throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest("Updated", "Edited text", List.of(NoteTagEnum.BUSINESS));
        NoteDto dto = new NoteDto("edit-id", "Updated", "Edited text", request.tags(), Instant.parse("2024-07-01T00:00:00Z"));
        when(noteService.updateNote(any(UpdateNoteRequest.class), anyString())).thenReturn(dto);

        mockMvc.perform(patch("/api/notes/{id}", "edit-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("edit-id"))
                .andExpect(jsonPath("$.title").value("Updated"));

        verify(noteService).updateNote(any(UpdateNoteRequest.class), eq("edit-id"));
    }

    @Test
    void updateNote_rejectsEmptyFields() throws Exception {
        String payload = """
                {
                  "title": "",
                  "text": ""
                }
                """;

        mockMvc.perform(patch("/api/notes/{id}", "edit-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("size must be between 1 and 2147483647"))
                .andExpect(jsonPath("$.text").value("size must be between 1 and 2147483647"));
    }

    @Test
    void deleteNote_deletesResource() throws Exception {
        mockMvc.perform(delete("/api/notes/{id}", "del-id"))
                .andExpect(status().isOk());

        verify(noteService).deleteNote("del-id");
    }
}
