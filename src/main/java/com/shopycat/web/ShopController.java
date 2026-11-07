package com.shopycat.web;

import com.shopycat.model.AppUser;
import com.shopycat.model.Category;
import com.shopycat.model.Shop;
import com.shopycat.repo.OfferRepository;
import com.shopycat.repo.ShopRepository;
import com.shopycat.repo.UsedListingRepository;
import com.shopycat.security.CurrentUser;
import com.shopycat.service.PrivacyPolicy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
class ShopController {

    private final ShopRepository shops;
    private final OfferRepository offers;
    private final UsedListingRepository listings;
    private final CurrentUser currentUser;
    private final PrivacyPolicy privacy;

    ShopController(ShopRepository shops, OfferRepository offers, UsedListingRepository listings,
                   CurrentUser currentUser, PrivacyPolicy privacy) {
        this.shops = shops;
        this.offers = offers;
        this.listings = listings;
        this.currentUser = currentUser;
        this.privacy = privacy;
    }

    @GetMapping("/shops/new")
    String newShop() {
        return "shop-form";
    }

    @PostMapping("/shops")
    String create(@RequestParam String name,
                  @RequestParam Category category,
                  @RequestParam(required = false) String description,
                  @RequestParam(required = false) String address,
                  @RequestParam(required = false) String phone,
                  @RequestParam(required = false) String openingHours,
                  @RequestParam(required = false) String latitude,
                  @RequestParam(required = false) String longitude,
                  Model model) {
        AppUser me = currentUser.require();
        Double lat;
        Double lng;
        try {
            lat = Forms.toDouble(latitude);
            lng = Forms.toDouble(longitude);
        } catch (NumberFormatException e) {
            lat = null;
            lng = null;
        }
        String error = null;
        if (Forms.blank(name)) {
            error = "Please enter the shop name.";
        } else if (!Forms.validLatLng(lat, lng)) {
            error = "Please set the shop location (use the button while you are at the shop, or type it in).";
        }
        if (error != null) {
            model.addAttribute("error", error);
            return "shop-form";
        }
        Shop shop = shops.save(new Shop(me, name.trim(), category, Forms.trimOrNull(description),
                Forms.trimOrNull(address), Forms.trimOrNull(phone), Forms.trimOrNull(openingHours), lat, lng));
        return "redirect:/shops/" + shop.getId();
    }

    @GetMapping("/shops/{id}")
    String show(@PathVariable Long id, Model model) {
        Shop shop = shops.findById(id).orElseThrow(NotFoundException::new);
        AppUser viewer = currentUser.getOrNull();
        LocalDate today = LocalDate.now();
        model.addAttribute("shop", shop);
        model.addAttribute("isOwner", shop.getOwner().sameAs(viewer));
        model.addAttribute("offers", offers.findByShopOrderByCreatedAtDesc(shop).stream()
                .filter(o -> o.isActive(today) && privacy.canView(o, viewer)).toList());
        model.addAttribute("listings", listings.findByShopOrderByCreatedAtDesc(shop).stream()
                .filter(l -> l.isAvailable() && privacy.canView(l, viewer)).toList());
        return "shop";
    }
}
