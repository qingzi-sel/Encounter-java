package com.encounter.controller.dto;

import java.util.Map;

/**
 * Generic request DTO for player actions.
 */
public class ActionRequest {

    private String targetRoomId;
    private Map<String, Double> attributes;
    private int bookType;
    private int wordId;
    private String itemType;
    private String targetKey;
    private double targetDelta;

    public String getTargetRoomId() { return targetRoomId; }
    public void setTargetRoomId(String targetRoomId) { this.targetRoomId = targetRoomId; }

    public Map<String, Double> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Double> attributes) { this.attributes = attributes; }

    public int getBookType() { return bookType; }
    public void setBookType(int bookType) { this.bookType = bookType; }

    public int getWordId() { return wordId; }
    public void setWordId(int wordId) { this.wordId = wordId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getTargetKey() { return targetKey; }
    public void setTargetKey(String targetKey) { this.targetKey = targetKey; }

    public double getTargetDelta() { return targetDelta; }
    public void setTargetDelta(double targetDelta) { this.targetDelta = targetDelta; }
}
