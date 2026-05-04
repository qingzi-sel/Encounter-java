package com.encounter.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for an ongoing book reading mini-game.
 */
public class ReadingData {

    private int bookType; // 20 or 50
    private double timer;
    private double corruption;
    private double spawnTimer;
    private List<Word> words;

    public ReadingData() {}

    public ReadingData(int bookType, List<Word> words) {
        this.bookType = bookType;
        this.timer = 0.0;
        this.corruption = 0.0;
        this.spawnTimer = 0.0;
        this.words = new ArrayList<>(words);
    }

    public int getBookType() { return bookType; }
    public void setBookType(int bookType) { this.bookType = bookType; }

    public double getTimer() { return timer; }
    public void setTimer(double timer) { this.timer = timer; }

    public double getCorruption() { return corruption; }
    public void setCorruption(double corruption) { this.corruption = corruption; }

    public double getSpawnTimer() { return spawnTimer; }
    public void setSpawnTimer(double spawnTimer) { this.spawnTimer = spawnTimer; }

    public List<Word> getWords() { return words; }
    public void setWords(List<Word> words) { this.words = words; }

    /**
     * A text segment that may be corrupted.
     */
    public static class Word {
        private int id;
        private String text;
        private boolean corrupt;
        private double rot;

        public Word() {}

        public Word(int id, String text, double rot) {
            this.id = id;
            this.text = text;
            this.corrupt = false;
            this.rot = rot;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public boolean isCorrupt() { return corrupt; }
        public void setCorrupt(boolean corrupt) { this.corrupt = corrupt; }

        public double getRot() { return rot; }
        public void setRot(double rot) { this.rot = rot; }
    }
}
