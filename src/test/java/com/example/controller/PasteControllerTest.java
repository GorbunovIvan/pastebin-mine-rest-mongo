package com.example.controller;

import com.example.model.Paste;
import com.example.model.PasteRequest;
import com.example.service.PasteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PasteService pasteService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String baseURL = "/api/v1/pastes";
    private List<Paste> pastes;

    @BeforeEach
    void setUp() {

        pastes = List.of(
                PasteRequest.builder().text("first test message").access("PUBLIC").validityInSeconds(600).build().toPaste(),
                PasteRequest.builder().text("second test message").access("unlisted").validityInSeconds(700).build().toPaste(),
                PasteRequest.builder().text("third test message").access("public").validityInSeconds(0).build().toPaste(),
                PasteRequest.builder().text("forth test message").access("PUBLIC").validityInSeconds(0).build().toPaste()
        );

        when(pasteService.getByHash(0L)).thenReturn(null);
        when(pasteService.getAllLast()).thenReturn(pastes);
        when(pasteService.getAllBySubstring("-")).thenReturn(Collections.emptyList());
        when(pasteService.create(any(PasteRequest.class))).thenReturn(Paste.builder().build());

        for (var paste : pastes) {
            when(pasteService.getByHash(paste.getHash())).thenReturn(paste);
            when(pasteService.getAllBySubstring(paste.getText())).thenReturn(List.of(paste));
        }
    }

    @Test
    void testGetByHash() throws Exception {

        for (var paste : pastes) {

            mvc.perform(MockMvcRequestBuilders.get(baseURL + "/{hash}", paste.getHash()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(paste.getText()));

            verify(pasteService, times(1)).getByHash(paste.getHash());
        }

        mvc.perform(MockMvcRequestBuilders.get(baseURL + "/{hash}", 0))
                .andExpect(status().isNotFound());

        verify(pasteService, times(1)).getByHash(0L);

        mvc.perform(MockMvcRequestBuilders.get(baseURL + "/{hash}", "-"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllLast() throws Exception {

        String jsonResponse = mvc.perform(MockMvcRequestBuilders.get(baseURL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var typeFactory = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
        List<String> textsReturned = objectMapper.readValue(jsonResponse, typeFactory);

        assertFalse(textsReturned.isEmpty());

        verify(pasteService, times(1)).getAllLast();
    }

    @Test
    void testGetAllBySubstring() throws Exception {

        for (var paste : pastes) {

            String jsonResponse = mvc.perform(MockMvcRequestBuilders.get(baseURL + "/text/{text}", paste.getText()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            var typeFactory = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
            List<String> textsReturned = objectMapper.readValue(jsonResponse, typeFactory);

            assertFalse(textsReturned.isEmpty());
            assertTrue(textsReturned.contains(paste.getText()));

            verify(pasteService, times(1)).getAllBySubstring(paste.getText());
        }

        mvc.perform(MockMvcRequestBuilders.get(baseURL + "/text/{text}", "-"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(pasteService, times(1)).getAllBySubstring("-");
    }

    @Test
    void testCreate() throws Exception {

        var pasteRequest = PasteRequest.builder()
                .text("new message")
                .access("PUBLIC")
                .validityInSeconds(100)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(pasteRequest);

        mvc.perform(MockMvcRequestBuilders.post(baseURL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        verify(pasteService, times(1)).create(any(PasteRequest.class));
    }
}