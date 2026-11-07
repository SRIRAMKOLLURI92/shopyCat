package com.shopycat.model;

public enum ItemCondition {
    LIKE_NEW("Like new"),
    GOOD("Good"),
    FAIR("Fair"),
    FOR_PARTS("For parts / not working");

    private final String label;

    ItemCondition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
