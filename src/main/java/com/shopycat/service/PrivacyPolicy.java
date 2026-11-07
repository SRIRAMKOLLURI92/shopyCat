package com.shopycat.service;

import com.shopycat.model.AppUser;
import com.shopycat.model.Post;
import com.shopycat.model.Shop;
import com.shopycat.model.UsedListing;
import org.springframework.stereotype.Component;

/**
 * Every privacy rule between shop owners and users lives here, so it is easy to read and test.
 *
 * <ol>
 *   <li>Audience: each post picks who can see it (everyone, members, shop owners only, users only).
 *       The author always sees their own posts.</li>
 *   <li>Names: people are shown only by display name. Email and phone are never shown on a post.</li>
 *   <li>Location: shop posts use the shop's exact location (it is a business). Posts by a person
 *       are stored with the location rounded to about 1 km, so nobody can find their home.</li>
 *   <li>Contact: a shop's phone is public. A person's contact details are only shared after they
 *       accept a contact request, and then both sides see each other's details.</li>
 *   <li>Ownership: only the author can edit, delete or mark a post as sold, and only a shop's owner
 *       can post on behalf of that shop.</li>
 *   <li>Trust labels: offers from a shop are marked "Verified shop offer"; offers posted by users are
 *       marked "Community tip" so shoppers know who is behind them.</li>
 * </ol>
 */
@Component
public class PrivacyPolicy {

    /** Rule 1: can this viewer (null for a visitor) see the post? */
    public boolean canView(Post post, AppUser viewer) {
        if (post.getAuthor().sameAs(viewer)) {
            return true;
        }
        return switch (post.getAudience()) {
            case EVERYONE -> true;
            case MEMBERS -> viewer != null;
            case SHOP_OWNERS -> viewer != null && viewer.isShopOwner();
            case USERS -> viewer != null && !viewer.isShopOwner();
        };
    }

    /** Rule 3: the coordinates to store for a new post. */
    public double[] coordinatesToStore(Shop shop, double latitude, double longitude) {
        if (shop != null) {
            return new double[] {shop.getLatitude(), shop.getLongitude()};
        }
        return new double[] {Geo.blur(latitude), Geo.blur(longitude)};
    }

    /** Rule 4: may the viewer send a contact request for this listing? */
    public boolean canRequestContact(UsedListing listing, AppUser viewer) {
        return viewer != null
                && !listing.getAuthor().sameAs(viewer)
                && !listing.isFromShop()
                && listing.isAvailable()
                && canView(listing, viewer);
    }

    /**
     * Rule 4: may the viewer see the seller's personal contact details?
     *
     * @param acceptedRequest whether the seller accepted this viewer's contact request
     */
    public boolean canSeeSellerContact(UsedListing listing, AppUser viewer, boolean acceptedRequest) {
        if (viewer == null || !canView(listing, viewer)) {
            return false;
        }
        return listing.getAuthor().sameAs(viewer) || acceptedRequest;
    }

    /** Rule 5: may the viewer edit, delete or mark this post as sold? */
    public boolean canManage(Post post, AppUser viewer) {
        return post.getAuthor().sameAs(viewer);
    }

    /** Rule 5: may this user post on behalf of the shop? */
    public boolean canPostForShop(Shop shop, AppUser user) {
        return shop != null && user != null && user.isShopOwner() && shop.getOwner().sameAs(user);
    }
}
