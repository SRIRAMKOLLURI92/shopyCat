package com.shopycat.web;

import com.shopycat.model.Category;
import com.shopycat.model.Shop;
import com.shopycat.repo.ShopRepository;
import com.shopycat.security.CurrentUser;
import com.shopycat.service.FeedService;
import com.shopycat.service.Geo;
import com.shopycat.service.Nearby;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

/** The public "near me" page with three tabs: offers, used products and shops. */
@Controller
class HomeController {

    private final FeedService feed;
    private final ShopRepository shops;
    private final CurrentUser currentUser;

    HomeController(FeedService feed, ShopRepository shops, CurrentUser currentUser) {
        this.feed = feed;
        this.shops = shops;
        this.currentUser = currentUser;
    }

    @GetMapping("/")
    String home(@RequestParam(required = false) Double lat,
                @RequestParam(required = false) Double lng,
                @RequestParam(defaultValue = "5") double radius,
                @RequestParam(required = false) Category category,
                @RequestParam(required = false) String q,
                @RequestParam(defaultValue = "offers") String tab,
                Model model) {
        if (!Forms.validLatLng(lat, lng)) {
            lat = null;
            lng = null;
        }
        var filter = new FeedService.Filter(lat, lng, radius, category, q);
        var viewer = currentUser.getOrNull();

        switch (tab) {
            case "used" -> model.addAttribute("listings", feed.listings(filter, viewer));
            case "shops" -> model.addAttribute("shops", nearbyShops(lat, lng, radius, category));
            default -> {
                tab = "offers";
                model.addAttribute("offers", feed.offers(filter, viewer));
            }
        }
        model.addAttribute("tab", tab);
        model.addAttribute("lat", lat);
        model.addAttribute("lng", lng);
        model.addAttribute("radius", radius);
        model.addAttribute("category", category);
        model.addAttribute("q", q);
        return "home";
    }

    private List<Nearby<Shop>> nearbyShops(Double lat, Double lng, double radius, Category category) {
        boolean located = lat != null;
        return shops.findAll().stream()
                .filter(s -> category == null || s.getCategory() == category)
                .map(s -> new Nearby<>(s, located ? Geo.distanceKm(lat, lng, s.getLatitude(), s.getLongitude()) : null))
                .filter(n -> n.distanceKm() == null || n.distanceKm() <= radius)
                .sorted(located ? Comparator.comparingDouble(Nearby::distanceKm)
                        : Comparator.comparing((Nearby<Shop> n) -> n.item().getName()))
                .toList();
    }
}
