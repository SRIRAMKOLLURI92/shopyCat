package com.shopycat.web;

import com.shopycat.model.*;
import com.shopycat.repo.ContactRequestRepository;
import com.shopycat.repo.ShopRepository;
import com.shopycat.repo.UsedListingRepository;
import com.shopycat.security.CurrentUser;
import com.shopycat.service.PrivacyPolicy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/** Used (second-hand) products, plus the contact-request flow that protects sellers' details. */
@Controller
class ListingController {

    private final UsedListingRepository listings;
    private final ShopRepository shops;
    private final ContactRequestRepository contacts;
    private final CurrentUser currentUser;
    private final PrivacyPolicy privacy;

    ListingController(UsedListingRepository listings, ShopRepository shops, ContactRequestRepository contacts,
                      CurrentUser currentUser, PrivacyPolicy privacy) {
        this.listings = listings;
        this.shops = shops;
        this.contacts = contacts;
        this.currentUser = currentUser;
        this.privacy = privacy;
    }

    @GetMapping("/listings/new")
    String newListing(Model model) {
        model.addAttribute("myShops", myShops(currentUser.require()));
        return "listing-form";
    }

    @PostMapping("/listings")
    String create(@RequestParam String title,
                  @RequestParam(required = false) String description,
                  @RequestParam Category category,
                  @RequestParam Audience audience,
                  @RequestParam ItemCondition itemCondition,
                  @RequestParam String price,
                  @RequestParam(required = false) Long shopId,
                  @RequestParam(required = false) String latitude,
                  @RequestParam(required = false) String longitude,
                  Model model) {
        AppUser me = currentUser.require();
        Shop shop = null;
        if (shopId != null) {
            shop = shops.findById(shopId).orElse(null);
            if (!privacy.canPostForShop(shop, me)) {
                return fail(model, me, "You can only list items for your own shops.");
            }
        }
        BigDecimal amount;
        Double lat;
        Double lng;
        try {
            amount = Forms.toMoney(price);
            lat = Forms.toDouble(latitude);
            lng = Forms.toDouble(longitude);
        } catch (RuntimeException e) {
            return fail(model, me, "Some numbers are not valid. Please check them.");
        }
        if (Forms.blank(title)) {
            return fail(model, me, "Please enter a title.");
        }
        if (amount == null || amount.signum() < 0) {
            return fail(model, me, "Please enter a price (0 for free).");
        }
        if (shop == null && !Forms.validLatLng(lat, lng)) {
            return fail(model, me, "Please set your approximate location.");
        }
        double[] where = privacy.coordinatesToStore(shop, lat == null ? 0 : lat, lng == null ? 0 : lng);
        UsedListing listing = listings.save(new UsedListing(me, shop, title.trim(), Forms.trimOrNull(description),
                category, audience, where[0], where[1], amount, itemCondition));
        return "redirect:/listings/" + listing.getId();
    }

    @GetMapping("/listings/{id}")
    String show(@PathVariable Long id, Model model) {
        UsedListing listing = listings.findById(id).orElseThrow(NotFoundException::new);
        AppUser viewer = currentUser.getOrNull();
        if (!privacy.canView(listing, viewer)) {
            if (viewer == null) {
                return "redirect:/login";
            }
            throw new NotFoundException();
        }
        ContactRequest myRequest = viewer == null ? null
                : contacts.findByListingAndRequester(listing, viewer).orElse(null);
        boolean accepted = myRequest != null && myRequest.isAccepted();

        model.addAttribute("listing", listing);
        model.addAttribute("canManage", privacy.canManage(listing, viewer));
        model.addAttribute("canRequestContact", privacy.canRequestContact(listing, viewer) && myRequest == null);
        model.addAttribute("myRequest", myRequest);
        model.addAttribute("showSellerContact",
                !privacy.canManage(listing, viewer) && privacy.canSeeSellerContact(listing, viewer, accepted));
        return "listing";
    }

    @PostMapping("/listings/{id}/contact")
    String requestContact(@PathVariable Long id, @RequestParam(required = false) String message) {
        UsedListing listing = listings.findById(id).orElseThrow(NotFoundException::new);
        AppUser me = currentUser.require();
        if (privacy.canRequestContact(listing, me) && contacts.findByListingAndRequester(listing, me).isEmpty()) {
            String text = Forms.trimOrNull(message);
            if (text != null && text.length() > 500) {
                text = text.substring(0, 500);
            }
            contacts.save(new ContactRequest(listing, me, text));
        }
        return "redirect:/listings/" + id;
    }

    @PostMapping("/listings/{id}/sold")
    String markSold(@PathVariable Long id) {
        UsedListing listing = owned(id);
        listing.setStatus(ListingStatus.SOLD);
        listings.save(listing);
        return "redirect:/dashboard";
    }

    @PostMapping("/listings/{id}/delete")
    @Transactional
    String delete(@PathVariable Long id) {
        UsedListing listing = owned(id);
        contacts.deleteAll(contacts.findByListing(listing));
        listings.delete(listing);
        return "redirect:/dashboard";
    }

    @PostMapping("/contact/{requestId}/{decision}")
    String decide(@PathVariable Long requestId, @PathVariable String decision) {
        ContactRequest request = contacts.findById(requestId).orElseThrow(NotFoundException::new);
        if (!request.getListing().getAuthor().sameAs(currentUser.require())) {
            throw new NotFoundException();
        }
        switch (decision) {
            case "accept" -> request.setStatus(ContactStatus.ACCEPTED);
            case "decline" -> request.setStatus(ContactStatus.DECLINED);
            default -> throw new NotFoundException();
        }
        contacts.save(request);
        return "redirect:/dashboard";
    }

    private UsedListing owned(Long id) {
        UsedListing listing = listings.findById(id).orElseThrow(NotFoundException::new);
        if (!privacy.canManage(listing, currentUser.require())) {
            throw new NotFoundException();
        }
        return listing;
    }

    private String fail(Model model, AppUser me, String error) {
        model.addAttribute("error", error);
        model.addAttribute("myShops", myShops(me));
        return "listing-form";
    }

    private List<Shop> myShops(AppUser me) {
        return me.isShopOwner() ? shops.findByOwnerOrderByName(me) : List.of();
    }
}
