package com.shopycat.repo;

import com.shopycat.model.AppUser;
import com.shopycat.model.ListingStatus;
import com.shopycat.model.Shop;
import com.shopycat.model.UsedListing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsedListingRepository extends JpaRepository<UsedListing, Long> {
    List<UsedListing> findByAuthorOrderByCreatedAtDesc(AppUser author);
    List<UsedListing> findByStatus(ListingStatus status);
    List<UsedListing> findByShopOrderByCreatedAtDesc(Shop shop);
}
