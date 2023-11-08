package com.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pastes.remover.enabled", havingValue = "true")
public class PasteScheduledRemover {

    private final PasteService pasteService;

    @Scheduled(fixedRateString = "${pastes.remover.scheduled.fixed-rate.millis}")
    public void parseAndSend() {
        pasteService.removeExpiredPastes();
    }
}
