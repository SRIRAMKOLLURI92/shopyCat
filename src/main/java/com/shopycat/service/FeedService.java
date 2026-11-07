package com.shopycat.service;

import com.shopycat.model.*;
import com.shopycat.repo.OfferRepository;
import com.shopycat.repo.UsedListingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Builds the "near me" feeds. Filtering happens in Java to keep the code easy to follow;
 * when the data grows, move the distance filter into the database with PostGIS.
 */
@Service
public class FeedService {

    private final OfferRepository offers;
    private final UsedListingRepository listings;
    private final PrivacyPolicy privacy;

    public FeedService(OfferRepository offers, UsedListingRepository listings, PrivacyPolicy privacy) {
        this.offers = offers;
        this.listings = listings;
        this.privacy = privacy;
    }

    public record Filter(Double lat, Double lng, double radiusKm, Category category, String query) {
        boolean hasLocation() {
            return lat != null && lng != null;
        }
    }

    public List<Nearby<Offer>> offers(Filter filter, AppUser viewer) {
        LocalDate today = LocalDate.now();
        return build(offers.findAll(), filter, viewer, o -> o.isActive(today));
    }

    public List<Nearby<UsedListing>> listings(Filter filter, AppUser viewer) {
        return build(listings.findByStatus(ListingStatus.AVAILABLE), filter, viewer, l -> true);
    }

    private <T extends Post> List<Nearby<T>> build(List<T> posts, Filter filter, AppUser viewer, Predicate<T> extra) {
        String q = filter.query() == null ? "" : filter.query().trim().toLowerCase(Locale.ROOT);
        Comparator<Nearby<T>> order = filter.hasLocation()
                ? Comparator.comparingDouble(Nearby::distanceKm)
                : Comparator.comparing((Nearby<T> n) -> n.item().getCreatedAt()).reversed();
        return posts.stream()
                .filter(extra)
                .filter(p -> privacy.canView(p, viewer))
                .filter(p -> filter.category() == null || p.getCategory() == filter.category())
                .filter(p -> q.isEmpty() || matches(p, q))
                .map(p -> new Nearby<>(p, filter.hasLocation()
                        ? Geo.distanceKm(filter.lat(), filter.lng(), p.getLatitude(), p.getLongitude())
                        : null))
                .filter(n -> n.distanceKm() == null || n.distanceKm() <= filter.radiusKm())
                .sorted(order)
                .toList();
    }

    private static boolean matches(Post p, String q) {
        return p.getTitle().toLowerCase(Locale.ROOT).contains(q)
                || (p.getDescription() != null && p.getDescription().toLowerCase(Locale.ROOT).contains(q))
                || p.getPostedByName().toLowerCase(Locale.ROOT).contains(q);
    }
}
