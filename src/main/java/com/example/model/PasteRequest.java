package com.example.model;

import lombok.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Builder
@Getter @Setter
@EqualsAndHashCode
@ToString
public class PasteRequest {

    private String text;
    private String access;
    private long validityInSeconds;

    public Paste toPaste() {

        var createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        var expiredAt = createdAt.plusSeconds(this.getValidityInSeconds());
        var accessEnum = Access.valueOf(access.toUpperCase());

        var hash = Paste.generateHash(this.getText(), createdAt);

        return Paste.builder()
                .hash(hash)
                .text(this.getText())
                .access(accessEnum)
                .createdAt(createdAt)
                .expireAt(expiredAt)
                .build();
    }
}
