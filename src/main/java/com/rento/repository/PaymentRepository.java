package com.rento.repository;

import com.rento.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByRentalIdOrderByPaidAtAsc(Long rentalId);
}
