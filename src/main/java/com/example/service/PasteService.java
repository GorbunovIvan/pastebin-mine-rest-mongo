package com.example.service;

import com.example.model.Paste;
import com.example.model.PasteRequest;

import java.util.List;

/**
 * Service for interacting with paste storage
 */
public interface PasteService {

    /**
     * Returns a paste by a specified {@code hash}
     * @param hash a hash of a previously loaded paste
     * @return a paste with a specified {@code hash}
     */
    Paste getByHash(Long hash);

    /**
     * Returns the 10 most recently loaded public pastes
     * @return the 10 most recently loaded public pastes
     */
    List<Paste> getAllLast();

    /**
     * Returns all public pastes with text that has the specified substring ({@code text})
     * @param text substring for searching in pastes
     * @return all  pastes with text containing specified substring ({@code text})
     */
    List<Paste> getAllBySubstring(String text);

    /**
     * Adds new paste
     * @param pasteRequest request with specified paste parameters
     * @return a new paste created by specified parameters
     */
    Paste create(PasteRequest pasteRequest);

    /**
     * Removes all expired pastes
     */
    void removeExpiredPastes();
}
