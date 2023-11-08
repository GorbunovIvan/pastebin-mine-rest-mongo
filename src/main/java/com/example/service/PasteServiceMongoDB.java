package com.example.service;

import com.example.model.Access;
import com.example.model.Paste;
import com.example.model.PasteRequest;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasteServiceMongoDB implements PasteService {

    private final MongoDatabase db;

    @Value("${mongodb.collection.pastes}")
    private String collectionName;

    @Value("${mongodb.limit-of-last-pastes}")
    private int limitOfLastPastes;

    @Override
    public Paste getByHash(Long hash) {

        var collection = getCollectionOfPastes();

        var docs = collection.find(new Document("hash", hash));
        var doc = docs.first();

        return toPaste(doc);
    }

    @Override
    public List<Paste> getAllLast() {
        return getAllLast(limitOfLastPastes);
    }

    protected List<Paste> getAllLast(int limit) {

        var collection = getCollectionOfPastes();

        var docs = collection.find(new Document("access", Access.PUBLIC))
                .sort(Sorts.descending("createdAt"))
                .limit(limit);

        return toPastes(docs);
    }

    @Override
    public List<Paste> getAllBySubstring(String text) {

        if (Objects.requireNonNullElse(text, "").isBlank()) {
            return Collections.emptyList();
        }

        Pattern pattern = Pattern.compile(text, Pattern.CASE_INSENSITIVE);

        var collection = getCollectionOfPastes();

        var filter = new Document();
        filter.append("text", new Document("$regex", pattern));
        filter.append("access", Access.PUBLIC);

        var docs = collection.find(filter)
                .sort(Sorts.ascending("createdAt"));

        return toPastes(docs);
    }

    @Override
    public Paste create(PasteRequest pasteRequest) {

        var paste = pasteRequest.toPaste();

        var document = toDocument(paste);

        var collection = getCollectionOfPastes();
        collection.insertOne(document);

        log.info("New paste is added by hash '{}'", paste.getHash());

        return paste;
    }

    @Override
    public void removeExpiredPastes() {

        var collection = getCollectionOfPastes();

        var now = toDate(LocalDateTime.now());
        var query = new Document("expireAt", new Document("$lt", now));
        var deleteResult = collection.deleteMany(query);

        if (deleteResult.getDeletedCount() > 0) {
            log.info("Expired pastes were deleted: '{}' pastes", deleteResult.getDeletedCount());
        }
    }

    private MongoCollection<Document> getCollectionOfPastes() {
        return db.getCollection(collectionName);
    }

    private Paste toPaste(@Nullable Document document) {

        if (document == null) {
            return null;
        }

        var hash = document.getLong("hash");
        var text = document.getString("text");
        var access = Access.valueOf(document.getString("access"));
        var createdAt = toLocalDateTime(document.getDate("createdAt"));
        var expireAt = toLocalDateTime(document.getDate("expireAt"));

        return Paste.builder()
                .hash(hash)
                .text(text)
                .access(access)
                .createdAt(createdAt)
                .expireAt(expireAt)
                .build();
    }

    private List<Paste> toPastes(Iterable<Document> documents) {

        var pastes = new ArrayList<Paste>();

        for (var doc : documents) {
            var paste = toPaste(doc);
            pastes.add(paste);
        }

        return pastes;
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

    private LocalDateTime toLocalDateTime(Date date) {
        var instant = date.toInstant();
        return instant.atZone(zoneId()).toLocalDateTime();
    }

    private Date toDate(LocalDateTime localDateTime) {
        var zonedDateTime = localDateTime.atZone(zoneId());
        return Date.from(zonedDateTime.toInstant());
    }

    private ZoneId zoneId() {
        return ZoneId.systemDefault();
    }
}
