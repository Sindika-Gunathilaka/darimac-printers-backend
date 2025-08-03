package com.example.printingApp.model;

public enum SublimationType {
    MUGS("Mugs"),
    CRISTAL("Cristal"),
    GLASS("Glass"),
    BOTTLES("Bottles"),
    OTHER("Other");

    private final String displayName;

    SublimationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}