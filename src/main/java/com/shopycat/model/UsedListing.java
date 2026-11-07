package com.shopycat.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** A used (second-hand) product for sale, by a shop or by a person. */
@Entity
public class UsedListing extends Post {

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCondition itemCondition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.AVAILABLE;

    protected UsedListing() {
    }

    public UsedListing(AppUser author, Shop shop, String title, String description, Category category,
                       Audience audience, double latitude, double longitude,
                       BigDecimal price, ItemCondition itemCondition) {
        super(author, shop, title, description, category, audience, latitude, longitude);
        this.price = price;
        this.itemCondition = itemCondition;
    }

    public boolean isAvailable() {
        return status == ListingStatus.AVAILABLE;
    }

    public BigDecimal getPrice() { return price; }
    public ItemCondition getItemCondition() { return itemCondition; }
    public ListingStatus getStatus() { return status; }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }
}
