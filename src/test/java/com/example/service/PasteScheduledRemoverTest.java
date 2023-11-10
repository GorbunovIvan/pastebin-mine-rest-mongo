package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PasteScheduledRemoverTest {

    private PasteScheduledRemover pasteScheduledRemover;

    @Mock
    private PasteService pasteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pasteScheduledRemover = new PasteScheduledRemover(pasteService);
    }

    @Test
    void testParseAndSend() {
        pasteScheduledRemover.parseAndSend();
        verify(pasteService, times(1)).removeExpiredPastes();
    }
}