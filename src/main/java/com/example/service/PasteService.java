package com.example.service;

import com.example.model.Paste;
import com.example.model.PasteRequest;

import java.util.List;

public interface PasteService {
    Paste getByHash(Long hash);
    List<Paste> getAllLast();
    List<Paste> getAllBySubstring(String text);
    Paste create(PasteRequest pasteRequest);
    void removeExpiredPastes();
}
