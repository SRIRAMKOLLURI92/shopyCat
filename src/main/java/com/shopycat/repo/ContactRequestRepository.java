package com.shopycat.repo;

import com.shopycat.model.AppUser;
import com.shopycat.model.ContactRequest;
import com.shopycat.model.UsedListing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
    List<ContactRequest> findByListingAuthorOrderByCreatedAtDesc(AppUser seller);
    List<ContactRequest> findByRequesterOrderByCreatedAtDesc(AppUser requester);
    List<ContactRequest> findByListing(UsedListing listing);
    Optional<ContactRequest> findByListingAndRequester(UsedListing listing, AppUser requester);
}
