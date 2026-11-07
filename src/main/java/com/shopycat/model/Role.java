package com.shopycat.model;

/** The two kinds of accounts on shopyCat. */
public enum Role {
    /** Runs one or more shops and posts verified shop offers. */
    SHOP_OWNER,
    /** A shopper. Can post community deal tips and sell used items. */
    USER;

    public String label() {
        return this == SHOP_OWNER ? "Shop owner" : "User";
    }
}
