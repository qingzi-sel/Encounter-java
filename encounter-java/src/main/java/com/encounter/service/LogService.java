package com.encounter.service;

import com.encounter.engine.GameState;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages the game log (max 50 entries).
 */
@Service
public class LogService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_LOGS = 50;

    public void addLog(GameState state, String msg) {
        String timestamp = LocalTime.now().format(TF);
        state.getLogs().add("[" + timestamp + "] " + msg);
        while (state.getLogs().size() > MAX_LOGS) {
            state.getLogs().remove(0);
        }
    }
}
