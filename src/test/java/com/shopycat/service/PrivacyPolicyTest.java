package com.shopycat.service;

import com.shopycat.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PrivacyPolicyTest {

    private final PrivacyPolicy privacy = new PrivacyPolicy();

    private static AppUser user(long id, Role role) {
        AppUser u = new AppUser("u" + id + "@x.dev", "hash", "U" + id, "123", role);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private final AppUser owner = user(1, Role.SHOP_OWNER);
    private final AppUser otherOwner = user(2, Role.SHOP_OWNER);
    private final AppUser shopper = user(3, Role.USER);
    private final AppUser seller = user(4, Role.USER);
    private final Shop shop = new Shop(owner, "Shop", Category.GROCERY, null, null, null, null, 17.123456, 78.654321);

    private UsedListing personalListing(Audience audience) {
        return new UsedListing(seller, null, "Table", null, Category.HOME, audience, 17.12, 78.65,
                BigDecimal.TEN, ItemCondition.GOOD);
    }

    @Test
    void audienceRules() {
        UsedListing everyone = personalListing(Audience.EVERYONE);
        UsedListing members = personalListing(Audience.MEMBERS);
        UsedListing ownersOnly = personalListing(Audience.SHOP_OWNERS);
        UsedListing usersOnly = personalListing(Audience.USERS);

        assertThat(privacy.canView(everyone, null)).isTrue();
        assertThat(privacy.canView(members, null)).isFalse();
        assertThat(privacy.canView(members, shopper)).isTrue();
        assertThat(privacy.canView(ownersOnly, shopper)).isFalse();
        assertThat(privacy.canView(ownersOnly, owner)).isTrue();
        assertThat(privacy.canView(usersOnly, owner)).isFalse();
        assertThat(privacy.canView(usersOnly, shopper)).isTrue();
        // The author always sees their own post, even if the audience excludes their role.
        assertThat(privacy.canView(ownersOnly, seller)).isTrue();
    }

    @Test
    void personalLocationsAreBlurredButShopLocationsAreExact() {
        assertThat(privacy.coordinatesToStore(null, 17.385044, 78.486671)).containsExactly(17.39, 78.49);
        assertThat(privacy.coordinatesToStore(shop, 0, 0)).containsExactly(17.123456, 78.654321);
    }

    @Test
    void sellerContactOnlyAfterAcceptance() {
        UsedListing listing = personalListing(Audience.EVERYONE);
        assertThat(privacy.canSeeSellerContact(listing, null, false)).isFalse();
        assertThat(privacy.canSeeSellerContact(listing, shopper, false)).isFalse();
        assertThat(privacy.canSeeSellerContact(listing, shopper, true)).isTrue();
        assertThat(privacy.canRequestContact(listing, shopper)).isTrue();
        assertThat(privacy.canRequestContact(listing, seller)).isFalse();
        assertThat(privacy.canRequestContact(listing, null)).isFalse();

        listing.setStatus(ListingStatus.SOLD);
        assertThat(privacy.canRequestContact(listing, shopper)).isFalse();
    }

    @Test
    void cannotRequestContactForPostsHiddenFromYou() {
        assertThat(privacy.canRequestContact(personalListing(Audience.SHOP_OWNERS), shopper)).isFalse();
    }

    @Test
    void onlyTheShopOwnerPostsForTheShopAndOnlyAuthorsManagePosts() {
        assertThat(privacy.canPostForShop(shop, owner)).isTrue();
        assertThat(privacy.canPostForShop(shop, otherOwner)).isFalse();
        assertThat(privacy.canPostForShop(shop, shopper)).isFalse();

        UsedListing listing = personalListing(Audience.EVERYONE);
        assertThat(privacy.canManage(listing, seller)).isTrue();
        assertThat(privacy.canManage(listing, shopper)).isFalse();
        assertThat(privacy.canManage(listing, null)).isFalse();
    }
}
