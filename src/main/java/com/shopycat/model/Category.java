package com.shopycat.model;

/** Shop and product categories used for filtering. */
public enum Category {
    GROCERY("Grocery"),
    FASHION("Fashion & clothing"),
    ELECTRONICS("Electronics & mobiles"),
    PHARMACY("Pharmacy & health"),
    FOOD("Restaurants & food"),
    HOME("Home & furniture"),
    BEAUTY("Beauty & salon"),
    BOOKS("Books & stationery"),
    SPORTS("Sports & fitness"),
    OTHER("Other");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
