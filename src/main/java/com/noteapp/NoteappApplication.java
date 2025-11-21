package com.noteapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableMongoAuditing
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class NoteappApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteappApplication.class, args);
    }

}
