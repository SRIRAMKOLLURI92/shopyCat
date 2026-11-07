package com.shopycat.model;

/**
 * Who is allowed to see a post. The author always sees their own posts.
 * See {@link com.shopycat.service.PrivacyPolicy} for the rules.
 */
public enum Audience {
    EVERYONE("Everyone, including visitors who are not logged in"),
    MEMBERS("Only logged-in members"),
    SHOP_OWNERS("Only shop owners (for example, stock clearance between shops)"),
    USERS("Only users (shoppers)");

    private final String description;

    Audience(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
