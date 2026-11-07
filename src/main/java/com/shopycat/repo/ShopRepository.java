package com.shopycat.repo;

import com.shopycat.model.AppUser;
import com.shopycat.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByOwnerOrderByName(AppUser owner);
}
