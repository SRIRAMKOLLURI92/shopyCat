package com.shopycat.web;

import com.shopycat.model.*;
import com.shopycat.repo.OfferRepository;
import com.shopycat.repo.ShopRepository;
import com.shopycat.security.CurrentUser;
import com.shopycat.service.PrivacyPolicy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
class OfferController {

    private final OfferRepository offers;
    private final ShopRepository shops;
    private final CurrentUser currentUser;
    private final PrivacyPolicy privacy;

    OfferController(OfferRepository offers, ShopRepository shops, CurrentUser currentUser, PrivacyPolicy privacy) {
        this.offers = offers;
        this.shops = shops;
        this.currentUser = currentUser;
        this.privacy = privacy;
    }

    @GetMapping("/offers/new")
    String newOffer(Model model) {
        model.addAttribute("myShops", myShops(currentUser.require()));
        return "offer-form";
    }

    @PostMapping("/offers")
    String create(@RequestParam String title,
                  @RequestParam(required = false) String description,
                  @RequestParam Category category,
                  @RequestParam Audience audience,
                  @RequestParam(required = false) Long shopId,
                  @RequestParam(required = false) String placeName,
                  @RequestParam(required = false) String discountPercent,
                  @RequestParam(required = false) String offerPrice,
                  @RequestParam(required = false) String validUntil,
                  @RequestParam(required = false) String latitude,
                  @RequestParam(required = false) String longitude,
                  Model model) {
        AppUser me = currentUser.require();
        Shop shop = null;
        if (shopId != null) {
            shop = shops.findById(shopId).orElse(null);
            if (!privacy.canPostForShop(shop, me)) {
                return fail(model, me, "You can only post offers for your own shops.");
            }
        }

        Integer discount;
        BigDecimal price;
        LocalDate until;
        Double lat;
        Double lng;
        try {
            discount = Forms.toInt(discountPercent);
            price = Forms.toMoney(offerPrice);
            until = Forms.toDate(validUntil);
            lat = Forms.toDouble(latitude);
            lng = Forms.toDouble(longitude);
        } catch (RuntimeException e) {
            return fail(model, me, "Some numbers or dates are not valid. Please check them.");
        }

        if (Forms.blank(title)) {
            return fail(model, me, "Please enter a title.");
        }
        if (discount != null && (discount < 1 || discount > 100)) {
            return fail(model, me, "Discount must be between 1 and 100 percent.");
        }
        if (until != null && until.isBefore(LocalDate.now())) {
            return fail(model, me, "The end date is already in the past.");
        }
        if (shop == null) {
            if (Forms.blank(placeName)) {
                return fail(model, me, "Please say where this deal is (shop name or place).");
            }
            if (!Forms.validLatLng(lat, lng)) {
                return fail(model, me, "Please set the location of the deal.");
            }
        }

        double[] where = privacy.coordinatesToStore(shop, lat == null ? 0 : lat, lng == null ? 0 : lng);
        Offer offer = offers.save(new Offer(me, shop, title.trim(), Forms.trimOrNull(description), category, audience,
                where[0], where[1], discount, price, until, shop == null ? placeName.trim() : null));
        return "redirect:/offers/" + offer.getId();
    }

    @GetMapping("/offers/{id}")
    String show(@PathVariable Long id, Model model) {
        Offer offer = offers.findById(id).orElseThrow(NotFoundException::new);
        AppUser viewer = currentUser.getOrNull();
        if (!privacy.canView(offer, viewer)) {
            if (viewer == null) {
                return "redirect:/login";
            }
            throw new NotFoundException();
        }
        model.addAttribute("offer", offer);
        model.addAttribute("canManage", privacy.canManage(offer, viewer));
        return "offer";
    }

    @PostMapping("/offers/{id}/delete")
    String delete(@PathVariable Long id) {
        Offer offer = offers.findById(id).orElseThrow(NotFoundException::new);
        if (!privacy.canManage(offer, currentUser.require())) {
            throw new NotFoundException();
        }
        offers.delete(offer);
        return "redirect:/dashboard";
    }

    private String fail(Model model, AppUser me, String error) {
        model.addAttribute("error", error);
        model.addAttribute("myShops", myShops(me));
        return "offer-form";
    }

    private List<Shop> myShops(AppUser me) {
        return me.isShopOwner() ? shops.findByOwnerOrderByName(me) : List.of();
    }
}
