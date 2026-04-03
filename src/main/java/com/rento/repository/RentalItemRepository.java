package com.rento.repository;

import com.rento.model.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {
    List<RentalItem> findByRentalId(Long rentalId);
}
