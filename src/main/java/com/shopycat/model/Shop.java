package com.shopycat.model;

import jakarta.persistence.*;

/** A physical store. Its address, phone and exact location are public business information. */
@Entity
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AppUser owner;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(length = 1000)
    private String description;

    private String address;
    private String phone;
    private String openingHours;

    private double latitude;
    private double longitude;

    protected Shop() {
    }

    public Shop(AppUser owner, String name, Category category, String description, String address,
                String phone, String openingHours, double latitude, double longitude) {
        this.owner = owner;
        this.name = name;
        this.category = category;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.openingHours = openingHours;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public AppUser getOwner() { return owner; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getOpeningHours() { return openingHours; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
