package com.noteapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class NoteappApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteappApplication.class, args);
    }

}
