package com.encounter.domain.model;

public enum RoomId {
    LIVING_ROOM("起居室"),
    GREAT_HALL("大礼堂"),
    THRONE_ROOM("王座大厅"),
    MAIN_GATE("城堡大门"),
    ARMORY("军械库"),
    WATCHTOWER("瞭望台"),
    YARD("城门庭院"),
    GRAND_LIBRARY("大图书馆"),
    ALCHEMY_LAB("炼金室"),
    OBSERVATORY("占星塔"),
    DRESSING_ROOM("更衣室"),
    GUEST_QUARTERS("贵宾室"),
    CHAPEL("礼拜堂"),
    KITCHEN("厨房"),
    LORDS_CHAMBER("领主卧房"),
    DUNGEON("地牢"),
    BELL_TOWER("钟楼"),
    SHADOW_CORRIDOR("密道"),
    GREENHOUSE("温室废园"),
    WINE_CELLAR("酒窖");

    private final String displayName;

    RoomId(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
