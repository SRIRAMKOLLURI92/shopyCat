package com.shopycat.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Fields shared by offers and used-product listings.
 * A post either belongs to a shop (exact shop location, shop contact is public)
 * or to a person (location is blurred to about 1 km, contact is private).
 */
@MappedSuperclass
public abstract class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AppUser author;

    /** Null when posted by a person rather than on behalf of a shop. */
    @ManyToOne
    private Shop shop;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Audience audience = Audience.EVERYONE;

    private double latitude;
    private double longitude;

    private Instant createdAt = Instant.now();

    protected Post() {
    }

    protected Post(AppUser author, Shop shop, String title, String description, Category category,
                   Audience audience, double latitude, double longitude) {
        this.author = author;
        this.shop = shop;
        this.title = title;
        this.description = description;
        this.category = category;
        this.audience = audience;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isFromShop() {
        return shop != null;
    }

    /** Name shown publicly as the poster: the shop name, or the person's display name. */
    public String getPostedByName() {
        return shop != null ? shop.getName() : author.getDisplayName();
    }

    public Long getId() { return id; }
    public AppUser getAuthor() { return author; }
    public Shop getShop() { return shop; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Audience getAudience() { return audience; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Instant getCreatedAt() { return createdAt; }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
