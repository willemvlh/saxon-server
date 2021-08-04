package io.github.willemvlh.transformer.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsTest {

    @Test
    void getVersionNumber() {
        assertTrue(Utils.getVersionNumber().startsWith("2"));
    }
}