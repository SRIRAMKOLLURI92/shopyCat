package com.shopycat.web;

import com.shopycat.model.AppUser;
import com.shopycat.repo.*;
import com.shopycat.security.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** One dashboard for both roles. Shop owners also see their shops. */
@Controller
class DashboardController {

    private final CurrentUser currentUser;
    private final ShopRepository shops;
    private final OfferRepository offers;
    private final UsedListingRepository listings;
    private final ContactRequestRepository contacts;

    DashboardController(CurrentUser currentUser, ShopRepository shops, OfferRepository offers,
                        UsedListingRepository listings, ContactRequestRepository contacts) {
        this.currentUser = currentUser;
        this.shops = shops;
        this.offers = offers;
        this.listings = listings;
        this.contacts = contacts;
    }

    @GetMapping("/dashboard")
    String dashboard(Model model) {
        AppUser me = currentUser.require();
        if (me.isShopOwner()) {
            model.addAttribute("myShops", shops.findByOwnerOrderByName(me));
        }
        model.addAttribute("myOffers", offers.findByAuthorOrderByCreatedAtDesc(me));
        model.addAttribute("myListings", listings.findByAuthorOrderByCreatedAtDesc(me));
        model.addAttribute("incoming", contacts.findByListingAuthorOrderByCreatedAtDesc(me));
        model.addAttribute("outgoing", contacts.findByRequesterOrderByCreatedAtDesc(me));
        return "dashboard";
    }
}
