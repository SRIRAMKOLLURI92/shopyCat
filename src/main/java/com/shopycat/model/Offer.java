package com.shopycat.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A deal. Offers posted on behalf of a shop are "verified shop offers".
 * Offers posted by users are "community tips" about a deal they spotted.
 */
@Entity
public class Offer extends Post {

    /** Discount percentage, for example 20 for 20% off. Optional. */
    private Integer discountPercent;

    /** Offer price. Optional. */
    private BigDecimal offerPrice;

    /** Last day the offer is valid. Null means no end date. */
    private LocalDate validUntil;

    /** For community tips: where the deal is (a shop name or place). */
    private String placeName;

    protected Offer() {
    }

    public Offer(AppUser author, Shop shop, String title, String description, Category category,
                 Audience audience, double latitude, double longitude,
                 Integer discountPercent, BigDecimal offerPrice, LocalDate validUntil, String placeName) {
        super(author, shop, title, description, category, audience, latitude, longitude);
        this.discountPercent = discountPercent;
        this.offerPrice = offerPrice;
        this.validUntil = validUntil;
        this.placeName = placeName;
    }

    public boolean isActive(LocalDate today) {
        return validUntil == null || !validUntil.isBefore(today);
    }

    public Integer getDiscountPercent() { return discountPercent; }
    public BigDecimal getOfferPrice() { return offerPrice; }
    public LocalDate getValidUntil() { return validUntil; }
    public String getPlaceName() { return placeName; }
}
