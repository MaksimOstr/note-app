package com.noteapp.repository;

import com.noteapp.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    @Query(value = "{ '_id': ?0 }", fields = "{ 'text': 1 }")
    String findTextById(String id);
}
