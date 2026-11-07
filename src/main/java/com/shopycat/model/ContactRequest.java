package com.shopycat.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A request to get in touch with the seller of a used item. Contact details
 * (email and phone) of both sides are only revealed once the seller accepts.
 */
@Entity
public class ContactRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UsedListing listing;

    @ManyToOne(optional = false)
    private AppUser requester;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactStatus status = ContactStatus.PENDING;

    private Instant createdAt = Instant.now();

    protected ContactRequest() {
    }

    public ContactRequest(UsedListing listing, AppUser requester, String message) {
        this.listing = listing;
        this.requester = requester;
        this.message = message;
    }

    public Long getId() { return id; }
    public UsedListing getListing() { return listing; }
    public AppUser getRequester() { return requester; }
    public String getMessage() { return message; }
    public ContactStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isAccepted() {
        return status == ContactStatus.ACCEPTED;
    }

    public void setStatus(ContactStatus status) {
        this.status = status;
    }
}
