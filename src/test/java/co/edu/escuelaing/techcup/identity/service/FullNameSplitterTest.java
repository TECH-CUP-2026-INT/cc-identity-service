package co.edu.escuelaing.techcup.identity.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FullNameSplitterTest {

    private final FullNameSplitter splitter = new FullNameSplitter();

    @Test
    void splitsTwoWordName() {
        String[] result = splitter.split("Juan Perez");
        assertEquals("Juan", result[0]);
        assertEquals("Perez", result[1]);
    }

    @Test
    void splitsMultiWordName() {
        String[] result = splitter.split("Juan Carlos Perez Gomez");
        assertEquals("Juan", result[0]);
        assertEquals("Carlos Perez Gomez", result[1]);
    }

    @Test
    void handlesSingleWordName() {
        String[] result = splitter.split("Juan");
        assertEquals("Juan", result[0]);
        assertEquals("", result[1]);
    }
}