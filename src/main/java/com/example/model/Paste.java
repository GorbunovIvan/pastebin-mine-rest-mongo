package com.example.model;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@EqualsAndHashCode
@ToString
public class Paste {

    private Long hash;
    private String text;
    private Access access;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;

    protected static long generateHash(String text, LocalDateTime time) {
        return 31L * text.hashCode()
                + 31L * time.hashCode();
    }
}
