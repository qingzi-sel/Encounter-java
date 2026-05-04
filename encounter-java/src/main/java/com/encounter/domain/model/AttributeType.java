package com.encounter.domain.model;

public enum AttributeType {
    STAMINA("耐力"),
    STRENGTH("力量"),
    PATIENCE("耐心"),
    INTELLIGENCE("智力"),
    FOCUS("注意力");

    private final String displayName;

    AttributeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
