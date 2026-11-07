package com.shopycat.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * An account. Email and phone are private: they are never shown on posts,
 * and are only shared after the owner accepts a contact request.
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    /** The only name other people see. */
    @Column(nullable = false)
    private String displayName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Instant createdAt = Instant.now();

    protected AppUser() {
    }

    public AppUser(String email, String passwordHash, String displayName, String phone, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.phone = phone;
        this.role = role;
    }

    public boolean isShopOwner() {
        return role == Role.SHOP_OWNER;
    }

    public boolean sameAs(AppUser other) {
        return other != null && id != null && id.equals(other.id);
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}
