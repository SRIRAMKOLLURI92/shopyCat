package com.shopycat.repo;

import com.shopycat.model.AppUser;
import com.shopycat.model.Offer;
import com.shopycat.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByAuthorOrderByCreatedAtDesc(AppUser author);
    List<Offer> findByShopOrderByCreatedAtDesc(Shop shop);
}
