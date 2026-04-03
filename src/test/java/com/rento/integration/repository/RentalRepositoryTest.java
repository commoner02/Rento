package com.rento.integration.repository;

import com.rento.model.Rental;
import com.rento.model.User;
import com.rento.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RentalRepositoryTest {

    @Autowired private RentalRepository rentalRepository;
    @Autowired private TestEntityManager em;

    private User buyer;

    @BeforeEach
    void setUp() {
        buyer = em.persistAndFlush(User.builder()
                .email("buyer@test.com").password("hashed")
                .firstName("Buyer").lastName("Test")
                .role(User.Role.BUYER).build());
    }

    @Test
    void findByUser_ReturnsUserRentals() {
        em.persistAndFlush(Rental.builder()
                .user(buyer).status(Rental.Status.PENDING)
                .rentalDate(LocalDate.now()).returnDate(LocalDate.now().plusDays(3))
                .totalAmount(new BigDecimal("30.00")).build());

        List<Rental> result = rentalRepository.findByUser(buyer);
        assertThat(result).hasSize(1);
    }

    @Test
    void findByStatus_ReturnsMatchingStatus() {
        em.persistAndFlush(Rental.builder()
                .user(buyer).status(Rental.Status.PENDING)
                .rentalDate(LocalDate.now()).returnDate(LocalDate.now().plusDays(3))
                .totalAmount(new BigDecimal("30.00")).build());

        List<Rental> result = rentalRepository.findByStatus(Rental.Status.PENDING);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Rental.Status.PENDING);
    }

    @Test
    void findOverdueRentals_ReturnsOverdue() {
        em.persistAndFlush(Rental.builder()
                .user(buyer).status(Rental.Status.ACTIVE)
                .rentalDate(LocalDate.now().minusDays(5))
                .returnDate(LocalDate.now().minusDays(1))
                .totalAmount(new BigDecimal("30.00")).build());

        List<Rental> result = rentalRepository.findOverdueRentals(LocalDate.now());
        assertThat(result).hasSize(1);
    }
}