package com.shopycat.web;

import com.shopycat.model.*;
import com.shopycat.repo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End-to-end checks of the main pages and privacy rules, using the demo data. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppFlowTest {

    @Autowired MockMvc mvc;
    @Autowired UsedListingRepository listings;
    @Autowired OfferRepository offers;
    @Autowired ShopRepository shops;
    @Autowired ContactRequestRepository contacts;

    private UsedListing listing(String title) {
        return listings.findAll().stream().filter(l -> l.getTitle().equals(title)).findFirst().orElseThrow();
    }

    @Test
    void visitorsSeeOnlyPublicOffers() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("20% off all fruits")))
                .andExpect(content().string(not(containsString("Festive sale"))))          // members only
                .andExpect(content().string(not(containsString("Bulk rice for other shops")))); // shop owners only
    }

    @Test
    void shopOwnersSeeShopOnlyOffersButUsersDoNot() throws Exception {
        mvc.perform(get("/").with(user("owner2@shopycat.dev").roles("SHOP_OWNER")))
                .andExpect(content().string(containsString("Bulk rice for other shops")));
        mvc.perform(get("/").with(user("user@shopycat.dev").roles("USER")))
                .andExpect(content().string(containsString("Festive sale")))
                .andExpect(content().string(not(containsString("Bulk rice for other shops"))));
    }

    @Test
    void hiddenListingLooksNotFound() throws Exception {
        long id = listing("Kids cycle (age 6-9)").getId(); // users only
        mvc.perform(get("/listings/" + id).with(user("owner@shopycat.dev").roles("SHOP_OWNER")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/listings/" + id)).andExpect(redirectedUrl("/login"));
    }

    @Test
    void sellerContactIsHiddenUntilTheyAccept() throws Exception {
        UsedListing table = listing("Study table, 2 years old"); // sold by user@shopycat.dev
        var buyer = user("user2@shopycat.dev").roles("USER");

        mvc.perform(get("/listings/" + table.getId()).with(buyer))
                .andExpect(content().string(not(containsString("user@shopycat.dev"))))
                .andExpect(content().string(containsString("Send contact request")));

        mvc.perform(post("/listings/" + table.getId() + "/contact").param("message", "Still available?")
                        .with(buyer).with(csrf()))
                .andExpect(status().is3xxRedirection());
        ContactRequest request = contacts.findByListing(table).getFirst();
        assertThat(request.getStatus()).isEqualTo(ContactStatus.PENDING);

        // Someone other than the seller cannot accept it.
        mvc.perform(post("/contact/" + request.getId() + "/accept").with(buyer).with(csrf()))
                .andExpect(status().isNotFound());

        mvc.perform(post("/contact/" + request.getId() + "/accept")
                        .with(user("user@shopycat.dev").roles("USER")).with(csrf()))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/listings/" + table.getId()).with(buyer))
                .andExpect(content().string(containsString("user@shopycat.dev")));
    }

    @Test
    void personalListingLocationIsBlurred() throws Exception {
        mvc.perform(post("/listings").with(user("user@shopycat.dev").roles("USER")).with(csrf())
                        .param("title", "Old bicycle").param("category", "SPORTS").param("audience", "EVERYONE")
                        .param("itemCondition", "FAIR").param("price", "900")
                        .param("latitude", "17.384567").param("longitude", "78.481234"))
                .andExpect(status().is3xxRedirection());
        UsedListing saved = listing("Old bicycle");
        assertThat(saved.getLatitude()).isEqualTo(17.38);
        assertThat(saved.getLongitude()).isEqualTo(78.48);
        assertThat(saved.isFromShop()).isFalse();
    }

    @Test
    void cannotPostOffersForSomeoneElsesShop() throws Exception {
        Shop boutique = shops.findAll().stream().filter(s -> s.getName().equals("Meena Styles")).findFirst().orElseThrow();
        long before = offers.count();
        mvc.perform(post("/offers").with(user("owner@shopycat.dev").roles("SHOP_OWNER")).with(csrf())
                        .param("title", "Fake sale").param("category", "FASHION").param("audience", "EVERYONE")
                        .param("shopId", boutique.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("only post offers for your own shops")));
        assertThat(offers.count()).isEqualTo(before);
    }

    @Test
    void usersCannotOpenAddShopPage() throws Exception {
        mvc.perform(get("/shops/new").with(user("user@shopycat.dev").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner@shopycat.dev", roles = "SHOP_OWNER")
    void dashboardAndFormsRender() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(status().isOk())
                .andExpect(content().string(containsString("My shops")))
                .andExpect(content().string(containsString("Fresh Mart")));
        mvc.perform(get("/offers/new")).andExpect(status().isOk());
        mvc.perform(get("/listings/new")).andExpect(status().isOk());
        mvc.perform(get("/shops/new")).andExpect(status().isOk());
        mvc.perform(get("/").param("tab", "shops").param("lat", "17.385").param("lng", "78.4867"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("km away")));
    }

    @Test
    void registerAndLoginPagesRender() throws Exception {
        mvc.perform(get("/register")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(post("/register").with(csrf()).param("email", "new@x.dev").param("password", "short")
                        .param("displayName", "New").param("role", "USER"))
                .andExpect(content().string(containsString("at least 8 characters")));
        mvc.perform(post("/register").with(csrf()).param("email", "new@x.dev").param("password", "longenough")
                        .param("displayName", "New").param("role", "USER"))
                .andExpect(redirectedUrl("/login?registered"));
    }
}
