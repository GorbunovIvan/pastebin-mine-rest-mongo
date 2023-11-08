package com.example.controller;

import com.example.model.Paste;
import com.example.model.PasteRequest;
import com.example.service.PasteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pastes")
@RequiredArgsConstructor
public class PasteController {

    private final PasteService pasteService;

    @GetMapping("/{hash}")
    public ResponseEntity<?> getByHash(@PathVariable String hash) {

        long hashFormatted;
        try {
            hashFormatted = Long.parseLong(hash);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Provided hash has a wrong format");
        }

        var paste = pasteService.getByHash(hashFormatted);

        if (paste == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(paste.getText());
    }

    @GetMapping
    public ResponseEntity<?> getAllLast() {

        var pastes = pasteService.getAllLast().stream()
                .map(Paste::getText)
                .toList();

        return ResponseEntity.ok(pastes);
    }

    @GetMapping("/text/{text}")
    public ResponseEntity<?> getAllBySubstring(@PathVariable String text) {

        var pastes = pasteService.getAllBySubstring(text).stream()
                .map(Paste::getText)
                .toList();

        return ResponseEntity.ok(pastes);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PasteRequest pasteRequest) {
        var paste = pasteService.create(pasteRequest);
        return ResponseEntity.ok(paste.getHash());
    }
}
