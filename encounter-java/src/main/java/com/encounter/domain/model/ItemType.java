package com.encounter.domain.model;

public enum ItemType {
    ETHER_POTION("隐世药剂"),
    HOURGLASS("时光沙漏"),
    STRAW_DOLL("厄运稻草人");

    private final String displayName;

    ItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
