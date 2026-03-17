package com.rento.service;

import com.rento.dto.request.RentalRequest;
import com.rento.dto.request.ReturnRequest;
import com.rento.exception.InsufficientQuantityException;
import com.rento.exception.ResourceNotFoundException;
import com.rento.model.*;
import com.rento.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;

    public Rental createRental(RentalRequest request, String userEmail) {
        User user = userService.findByEmail(userEmail);

        for (RentalRequest.RentalItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) continue;
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));
            if (product.getAvailableQuantity() < itemReq.getQuantity())
                throw new InsufficientQuantityException("Not enough stock for: " + product.getName());
        }

        long days = ChronoUnit.DAYS.between(request.getRentalDate(), request.getReturnDate());
        if (days <= 0) throw new IllegalStateException("Return date must be after rental date.");

        Rental rental = Rental.builder()
                .user(user).rentalDate(request.getRentalDate())
                .returnDate(request.getReturnDate()).notes(request.getNotes())
                .status(Rental.Status.PENDING).build();

        BigDecimal total = BigDecimal.ZERO;
        List<RentalItem> items = new ArrayList<>();

        for (RentalRequest.RentalItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) continue;
            Product product = productRepository.findById(itemReq.getProductId()).get();
            BigDecimal lineTotal = product.getDailyRate()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()))
                    .multiply(BigDecimal.valueOf(days));
            total = total.add(lineTotal);
            product.setAvailableQuantity(product.getAvailableQuantity() - itemReq.getQuantity());
            productRepository.save(product);
            items.add(RentalItem.builder().product(product)
                    .quantity(itemReq.getQuantity()).dailyRateAtRental(product.getDailyRate()).build());
        }

        if (items.isEmpty()) throw new IllegalStateException("Please select at least one item.");
        rental.setTotalAmount(total);
        Rental saved = rentalRepository.save(rental);
        items.forEach(i -> i.setRental(saved));
        rentalItemRepository.saveAll(items);
        return saved;
    }

    @Transactional(readOnly = true)
    public Rental findById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Rental> findAll() { return rentalRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Rental> findByUser(String email) {
        return rentalRepository.findByUserOrderByCreatedAtDesc(userService.findByEmail(email));
    }

    @Transactional(readOnly = true)
    public List<Rental> findByStatus(Rental.Status status) {
        return rentalRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Rental> findOverdue() {
        return rentalRepository.findOverdueRentals(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Payment> findPaymentsByRental(Long rentalId) {
        return paymentRepository.findByRentalIdOrderByPaidAtAsc(rentalId);
    }

    public Rental payAdvance(Long id, BigDecimal amount) {
        Rental rental = findById(id);
        if (Boolean.TRUE.equals(rental.getAdvancePaymentDone()))
            throw new IllegalStateException("Advance already paid.");
        if (amount.compareTo(rental.getMinimumAdvance()) < 0)
            throw new IllegalStateException("Minimum advance is Tk " + rental.getMinimumAdvance());
        if (amount.compareTo(rental.getTotalAmount()) > 0)
            throw new IllegalStateException("Advance cannot exceed total amount.");

        String ref = "ADV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        paymentRepository.save(Payment.builder()
                .rental(rental).amount(amount).type("ADVANCE")
                .status("PAID").paidAt(LocalDateTime.now()).reference(ref).build());

        rental.setAdvancePaid(amount);
        rental.setAdvancePaymentDone(true);
        return rentalRepository.save(rental);
    }

    public Rental payBalance(Long id) {
        Rental rental = findById(id);
        if (rental.getStatus() != Rental.Status.COMPLETED)
            throw new IllegalStateException("Balance can only be paid after return is processed.");
        if (Boolean.TRUE.equals(rental.getBalancePaymentDone()))
            throw new IllegalStateException("Balance already paid.");

        String ref = "BAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        paymentRepository.save(Payment.builder()
                .rental(rental).amount(rental.getBalanceDue()).type("BALANCE")
                .status("PAID").paidAt(LocalDateTime.now()).reference(ref).build());

        rental.setBalancePaymentDone(true);
        return rentalRepository.save(rental);
    }

    public Rental confirmRental(Long id) {
        Rental rental = findById(id);
        if (rental.getStatus() != Rental.Status.PENDING)
            throw new IllegalStateException("Only PENDING rentals can be confirmed.");
        if (!Boolean.TRUE.equals(rental.getAdvancePaymentDone()))
            throw new IllegalStateException("Buyer must pay the advance before confirmation.");
        rental.setStatus(Rental.Status.CONFIRMED);
        return rentalRepository.save(rental);
    }

    public Rental activateRental(Long id) {
        Rental rental = findById(id);
        if (rental.getStatus() != Rental.Status.CONFIRMED)
            throw new IllegalStateException("Only CONFIRMED rentals can be activated.");
        rental.setStatus(Rental.Status.ACTIVE);
        return rentalRepository.save(rental);
    }

    public Rental cancelRental(Long id) {
        Rental rental = findById(id);
        if (rental.getStatus() == Rental.Status.COMPLETED || rental.getStatus() == Rental.Status.ACTIVE)
            throw new IllegalStateException("Cannot cancel a completed or active rental.");
        rentalItemRepository.findByRentalId(id).forEach(item -> {
            Product p = item.getProduct();
            p.setAvailableQuantity(p.getAvailableQuantity() + item.getQuantity());
            productRepository.save(p);
        });
        rental.setStatus(Rental.Status.CANCELLED);
        return rentalRepository.save(rental);
    }

    public Rental processReturn(Long id, ReturnRequest returnRequest) {
        Rental rental = findById(id);
        if (rental.getStatus() != Rental.Status.ACTIVE && rental.getStatus() != Rental.Status.CONFIRMED)
            throw new IllegalStateException("Rental must be ACTIVE or CONFIRMED to process return.");

        BigDecimal totalDamage = BigDecimal.ZERO;
        if (returnRequest != null && returnRequest.getItems() != null) {
            for (ReturnRequest.ReturnItemRequest req : returnRequest.getItems()) {
                final BigDecimal[] dmg = {BigDecimal.ZERO};
                rentalItemRepository.findById(req.getRentalItemId()).ifPresent(item -> {
                    item.setReturnedQuantity(req.getReturnedQuantity() != null ? req.getReturnedQuantity() : item.getQuantity());
                    item.setConditionNotes(req.getConditionNotes());
                    BigDecimal fee = req.getDamageFee() != null ? req.getDamageFee() : BigDecimal.ZERO;
                    item.setDamageFee(fee);
                    dmg[0] = fee;
                    Product p = item.getProduct();
                    p.setAvailableQuantity(p.getAvailableQuantity() + item.getReturnedQuantity());
                    productRepository.save(p);
                    rentalItemRepository.save(item);
                });
                totalDamage = totalDamage.add(dmg[0]);
            }
        } else {
            rentalItemRepository.findByRentalId(id).forEach(item -> {
                item.setReturnedQuantity(item.getQuantity());
                Product p = item.getProduct();
                p.setAvailableQuantity(p.getAvailableQuantity() + item.getQuantity());
                productRepository.save(p);
                rentalItemRepository.save(item);
            });
        }

        rental.setDamageFee(totalDamage);
        rental.setActualReturnDate(LocalDate.now());
        rental.setStatus(Rental.Status.COMPLETED);
        return rentalRepository.save(rental);
    }

    public BigDecimal calculateTotal(List<RentalRequest.RentalItemRequest> items, long days) {
        BigDecimal total = BigDecimal.ZERO;
        for (RentalRequest.RentalItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            total = total.add(product.getDailyRate()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .multiply(BigDecimal.valueOf(days)));
        }
        return total;
    }

    public long getTotalRevenue() {
        return rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == Rental.Status.COMPLETED)
                .mapToLong(r -> r.getTotalAmount() != null ? r.getTotalAmount().longValue() : 0)
                .sum();
    }
}
