package com.encounter.domain.model;

public enum TarotCard {
    HERMIT("《 隐 者 》"),
    WHEEL("《 命 运 之 轮 》"),
    HANGED("《 倒 吊 人 》"),
    TOWER("《 高 塔 》");

    private final String displayName;

    TarotCard(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
