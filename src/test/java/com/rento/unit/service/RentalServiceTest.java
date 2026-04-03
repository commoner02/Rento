package com.rento.unit.service;

import com.rento.dto.request.RentalRequest;
import com.rento.exception.InsufficientQuantityException;
import com.rento.model.*;
import com.rento.repository.*;
import com.rento.service.RentalService;
import com.rento.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock private RentalRepository rentalRepository;
    @Mock private RentalItemRepository rentalItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private UserService userService;
    @InjectMocks private RentalService rentalService;

    private User buyer;
    private Product chair;
    private Rental pendingRental;

    @BeforeEach
    void setUp() {
        buyer = User.builder().id(1L).email("buyer@test.com").role(User.Role.BUYER).build();

        chair = Product.builder()
                .id(1L).name("Folding Chair")
                .dailyRate(new BigDecimal("2.50"))
                .availableQuantity(10)
                .build();

        pendingRental = Rental.builder()
                .id(1L).user(buyer).status(Rental.Status.PENDING)
                .rentalDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(3))
                .totalAmount(new BigDecimal("75.00"))
                .advancePaid(BigDecimal.ZERO)
                .advancePaymentDone(false)
                .build();
    }

    @Test
    void createRental_ValidRequest_CreatesRental() {
        RentalRequest request = new RentalRequest();
        request.setRentalDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(3));

        RentalRequest.RentalItemRequest item = new RentalRequest.RentalItemRequest();
        item.setProductId(1L);
        item.setQuantity(3);
        request.setItems(List.of(item));

        when(userService.findByEmail(any())).thenReturn(buyer);
        when(productRepository.findById(1L)).thenReturn(Optional.of(chair));
        when(rentalRepository.save(any())).thenAnswer(inv -> {
            Rental r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        Rental result = rentalService.createRental(request, "buyer@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Rental.Status.PENDING);
        verify(rentalRepository).save(any());
    }

    @Test
    void createRental_InsufficientQuantity_ThrowsException() {
        RentalRequest request = new RentalRequest();
        request.setRentalDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(3));

        RentalRequest.RentalItemRequest item = new RentalRequest.RentalItemRequest();
        item.setProductId(1L);
        item.setQuantity(20); // Only 10 available
        request.setItems(List.of(item));

        when(userService.findByEmail(any())).thenReturn(buyer);
        when(productRepository.findById(1L)).thenReturn(Optional.of(chair));

        assertThatThrownBy(() -> rentalService.createRental(request, "buyer@test.com"))
                .isInstanceOf(InsufficientQuantityException.class);
    }

    @Test
    void payAdvance_ValidAmount_SetsAdvancePaid() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(pendingRental));
        when(rentalRepository.save(any())).thenReturn(pendingRental);

        Rental result = rentalService.payAdvance(1L, new BigDecimal("20.00"));

        assertThat(result.getAdvancePaymentDone()).isTrue();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void confirmRental_WithAdvancePaid_Confirms() {
        pendingRental.setAdvancePaymentDone(true);
        pendingRental.setAdvancePaid(new BigDecimal("20.00"));

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(pendingRental));
        when(rentalRepository.save(any())).thenReturn(pendingRental);

        Rental result = rentalService.confirmRental(1L);

        assertThat(result.getStatus()).isEqualTo(Rental.Status.CONFIRMED);
    }

    @Test
    void cancelRental_PendingRental_Cancels() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(pendingRental));
        when(rentalItemRepository.findByRentalId(1L)).thenReturn(List.of());
        when(rentalRepository.save(any())).thenReturn(pendingRental);

        Rental result = rentalService.cancelRental(1L);

        assertThat(result.getStatus()).isEqualTo(Rental.Status.CANCELLED);
    }

    @Test
    void processReturn_ActiveRental_Completes() {
        Rental activeRental = Rental.builder()
                .id(2L).user(buyer).status(Rental.Status.ACTIVE)
                .rentalDate(LocalDate.now()).returnDate(LocalDate.now().plusDays(3))
                .build();

        when(rentalRepository.findById(2L)).thenReturn(Optional.of(activeRental));
        when(rentalItemRepository.findByRentalId(2L)).thenReturn(List.of());
        when(rentalRepository.save(any())).thenReturn(activeRental);

        Rental result = rentalService.processReturn(2L, null);

        assertThat(result.getStatus()).isEqualTo(Rental.Status.COMPLETED);
        assertThat(result.getActualReturnDate()).isNotNull();
    }
}