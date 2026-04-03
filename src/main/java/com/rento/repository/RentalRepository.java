package com.rento.repository;

import com.rento.model.Rental;
import com.rento.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUser(User user);
    List<Rental> findByStatus(Rental.Status status);

    @Query("SELECT r FROM Rental r WHERE r.status = 'ACTIVE' AND r.returnDate < :today")
    List<Rental> findOverdueRentals(@Param("today") LocalDate today);

    List<Rental> findByUserOrderByCreatedAtDesc(User user);
}
