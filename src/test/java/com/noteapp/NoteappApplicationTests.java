package com.noteapp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Context test requires a running MongoDB instance")
@SpringBootTest
class NoteappApplicationTests {

    @Test
    void contextLoads() {
    }
}
