package com.example.service;

import com.example.model.Access;
import com.example.model.Paste;
import com.example.model.PasteRequest;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PasteServiceMongoDBTest {

    @Autowired
    private PasteServiceMongoDB pasteServiceMongoDB;

    @SpyBean
    private MongoDatabase db;

    @Value("${mongodb.collection.pastes}")
    private String collectionName;

    @Value("${pastes.last-pastes.limit}")
    private int limitOfLastPastes;

    private List<Paste> pastes;

    @BeforeEach
    void setUp() {

        pastes = new ArrayList<>(List.of(
                PasteRequest.builder().text("first test message").access("PUBLIC").validityInSeconds(600).build().toPaste(),
                PasteRequest.builder().text("second test message").access("unlisted").validityInSeconds(700).build().toPaste(),
                PasteRequest.builder().text("third test message").access("public").validityInSeconds(0).build().toPaste(),
                PasteRequest.builder().text("forth test message").access("PUBLIC").validityInSeconds(0).build().toPaste()
        ));

        var docs = toDocuments(pastes);

        var collection = db.getCollection(collectionName);
        collection.insertMany(docs);
    }

    @AfterEach
    void tearDown() {
        var collection = db.getCollection(collectionName);
        collection.drop();
    }

    @Test
    void testGetByHash() {
        for (var paste : pastes) {
            assertEquals(paste, pasteServiceMongoDB.getByHash(paste.getHash()));
        }
        assertNull(pasteServiceMongoDB.getByHash(0L));
    }

    @Test
    void testGetAllLast() {

        var pastesFound = pasteServiceMongoDB.getAllLast();
        assertNotEquals(pastes, pastesFound);

        var pastesExpected = pastes.stream()
                .filter(p -> p.getAccess().equals(Access.PUBLIC))
                .sorted(Comparator.comparing(Paste::getCreatedAt).reversed())
                .limit(limitOfLastPastes)
                .toList();

        assertEquals(pastesExpected, pastesFound);
    }

    @Test
    void testGetAllBySubstring() {

        assertTrue(pasteServiceMongoDB.getAllBySubstring("").isEmpty());
        assertTrue(pasteServiceMongoDB.getAllBySubstring("oooooooooo").isEmpty());

        // By words
        var words = pastes.stream()
                        .map(Paste::getText)
                        .map(s -> s.split("\\W"))
                        .flatMap(Arrays::stream)
                        .distinct()
                        .toList();

        for (var word : words) {

            var pastesExpected = pastes.stream()
                    .filter(p -> p.getAccess().equals(Access.PUBLIC))
                    .filter(p -> p.getText().contains(word))
                    .collect(Collectors.toSet());

            var pastesFound = new HashSet<>(pasteServiceMongoDB.getAllBySubstring(word));
            assertEquals(pastesExpected, pastesFound);
        }

        // By sub-words
        var subWords = new ArrayList<String>();

        for (var word : words) {
            int indexOfMiddle = word.length()/2;
            if (indexOfMiddle > 2) {
                subWords.add(word.substring(0, indexOfMiddle));
                subWords.add(word.substring(indexOfMiddle));
            } else {
                subWords.add(word);
            }
        }

        for (var word : subWords) {

            var pastesExpected = pastes.stream()
                    .filter(p -> p.getAccess().equals(Access.PUBLIC))
                    .filter(p -> p.getText().contains(word))
                    .collect(Collectors.toSet());

            var pastesFound = new HashSet<>(pasteServiceMongoDB.getAllBySubstring(word));
            assertEquals(pastesExpected, pastesFound);
        }
    }

    @Test
    void testCreate() {

        var pasteRequest = PasteRequest.builder().text("new message").access("PUBLIC").validityInSeconds(100).build();
        var pasteExpected = pasteRequest.toPaste();

        var pasteCreated = pasteServiceMongoDB.create(pasteRequest);

        assertEquals(pasteExpected.getText(), pasteCreated.getText());
        assertEquals(pasteExpected.getAccess(), pasteCreated.getAccess());

        assertEquals(pasteCreated, pasteServiceMongoDB.getByHash(pasteCreated.getHash()));
    }

    @Test
    void testRemoveExpiredPastes() {

        var pastesExpired = pastes.stream()
                            .filter(p -> !LocalDateTime.now().isBefore(p.getExpireAt()))
                            .toList();

        pasteServiceMongoDB.removeExpiredPastes();

        for (var pasteExpired : pastesExpired) {
            assertNull(pasteServiceMongoDB.getByHash(pasteExpired.getHash()));
        }

        pastes.removeAll(pastesExpired);
        for (var pasteValid : pastes) {
            assertEquals(pasteValid, pasteServiceMongoDB.getByHash(pasteValid.getHash()));
        }
    }

    private List<Document> toDocuments(Collection<Paste> pastes) {

        var documents = new ArrayList<Document>();

        for (var paste : pastes) {
            var document = toDocument(paste);
            documents.add(document);
        }

        return documents;
    }

    private Document toDocument(Paste paste) {

        if (paste == null) {
            return null;
        }

        var document = new Document();
        document.append("hash", paste.getHash());
        document.append("text", paste.getText());
        document.append("access", paste.getAccess());
        document.append("createdAt", toDate(paste.getCreatedAt()));
        document.append("expireAt", toDate(paste.getExpireAt()));

        return document;
    }

    private Date toDate(LocalDateTime localDateTime) {
        var zonedDateTime = localDateTime.atZone(zoneId());
        return Date.from(zonedDateTime.toInstant());
    }

    private ZoneId zoneId() {
        return ZoneId.systemDefault();
    }
}