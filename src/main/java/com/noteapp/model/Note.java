package com.noteapp.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Document("notes")
public class Note {
    @Id
    private String id;

    private String title;

    private String text;

    private List<NoteTagEnum> tags;

    @CreatedDate
    private Instant createdDate;
}
