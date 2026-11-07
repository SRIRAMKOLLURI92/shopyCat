package com.shopycat.config;

import com.shopycat.model.*;
import com.shopycat.repo.*;
import com.shopycat.service.PrivacyPolicy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Adds demo accounts, shops and posts the first time the app starts with an empty database.
 * Demo logins (password "password123"): owner@shopycat.dev (shop owner), user@shopycat.dev (user).
 * The demo area is central Hyderabad; change BASE_LAT/BASE_LNG to your own area.
 */
@Component
@ConditionalOnProperty(name = "shopycat.demo-data", havingValue = "true")
class DemoDataLoader implements CommandLineRunner {

    static final double BASE_LAT = 17.3850;
    static final double BASE_LNG = 78.4867;

    private final AppUserRepository users;
    private final ShopRepository shops;
    private final OfferRepository offers;
    private final UsedListingRepository listings;
    private final PasswordEncoder encoder;
    private final PrivacyPolicy privacy;

    DemoDataLoader(AppUserRepository users, ShopRepository shops, OfferRepository offers,
                   UsedListingRepository listings, PasswordEncoder encoder, PrivacyPolicy privacy) {
        this.users = users;
        this.shops = shops;
        this.offers = offers;
        this.listings = listings;
        this.encoder = encoder;
        this.privacy = privacy;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        String pw = encoder.encode("password123");
        AppUser owner = users.save(new AppUser("owner@shopycat.dev", pw, "Ravi (Fresh Mart)", "+91 90000 00001", Role.SHOP_OWNER));
        AppUser owner2 = users.save(new AppUser("owner2@shopycat.dev", pw, "Meena Styles", "+91 90000 00002", Role.SHOP_OWNER));
        AppUser user = users.save(new AppUser("user@shopycat.dev", pw, "Anil", "+91 90000 00003", Role.USER));
        AppUser user2 = users.save(new AppUser("user2@shopycat.dev", pw, "Priya", "+91 90000 00004", Role.USER));

        Shop mart = shops.save(new Shop(owner, "Fresh Mart", Category.GROCERY, "Daily groceries, fruits and vegetables.",
                "Road No. 1, Abids", "+91 40 1111 2222", "7am - 10pm", BASE_LAT + 0.004, BASE_LNG + 0.003));
        Shop mobiles = shops.save(new Shop(owner, "Ravi Mobiles", Category.ELECTRONICS, "Phones, accessories and repairs.",
                "Main Bazaar", "+91 40 1111 3333", "10am - 9pm", BASE_LAT - 0.006, BASE_LNG + 0.008));
        Shop boutique = shops.save(new Shop(owner2, "Meena Styles", Category.FASHION, "Sarees, kurtas and kids wear.",
                "Station Road", "+91 40 2222 4444", "11am - 9pm", BASE_LAT + 0.012, BASE_LNG - 0.010));

        LocalDate inAWeek = LocalDate.now().plusDays(7);
        offers.save(new Offer(owner, mart, "20% off all fruits this weekend", "Fresh stock every morning.",
                Category.GROCERY, Audience.EVERYONE, mart.getLatitude(), mart.getLongitude(), 20, null, inAWeek, null));
        offers.save(new Offer(owner, mobiles, "Free tempered glass with any phone case", null,
                Category.ELECTRONICS, Audience.EVERYONE, mobiles.getLatitude(), mobiles.getLongitude(), null, null, inAWeek, null));
        offers.save(new Offer(owner2, boutique, "Festive sale: kurtas from Rs 499", "Sizes S to XXL.",
                Category.FASHION, Audience.MEMBERS, boutique.getLatitude(), boutique.getLongitude(), 30, new BigDecimal("499"), inAWeek, null));
        offers.save(new Offer(owner, mart, "Bulk rice for other shops at wholesale price", "Minimum 10 bags.",
                Category.GROCERY, Audience.SHOP_OWNERS, mart.getLatitude(), mart.getLongitude(), null, new BigDecimal("1150"), inAWeek, null));

        double[] tip = privacy.coordinatesToStore(null, BASE_LAT + 0.002, BASE_LNG - 0.004);
        offers.save(new Offer(user, null, "Buy 1 get 1 on ice cream at the corner bakery", "Spotted today, valid till stock lasts.",
                Category.FOOD, Audience.EVERYONE, tip[0], tip[1], 50, null, LocalDate.now().plusDays(2), "Sri Sai Bakery"));

        double[] anil = privacy.coordinatesToStore(null, BASE_LAT + 0.0031, BASE_LNG + 0.0012);
        listings.save(new UsedListing(user, null, "Study table, 2 years old", "Pick up only.",
                Category.HOME, Audience.EVERYONE, anil[0], anil[1], new BigDecimal("1500"), ItemCondition.GOOD));
        double[] priya = privacy.coordinatesToStore(null, BASE_LAT - 0.0083, BASE_LNG + 0.0021);
        listings.save(new UsedListing(user2, null, "Kids cycle (age 6-9)", null,
                Category.SPORTS, Audience.USERS, priya[0], priya[1], new BigDecimal("2200"), ItemCondition.LIKE_NEW));
        listings.save(new UsedListing(owner, mobiles, "Refurbished phone, 64 GB", "3 months shop warranty.",
                Category.ELECTRONICS, Audience.EVERYONE, mobiles.getLatitude(), mobiles.getLongitude(), new BigDecimal("6999"), ItemCondition.GOOD));
        listings.save(new UsedListing(owner2, boutique, "Old display racks for other shops", "Clearing space before the festive season.",
                Category.OTHER, Audience.SHOP_OWNERS, boutique.getLatitude(), boutique.getLongitude(), new BigDecimal("800"), ItemCondition.FAIR));
    }
}
