package com.rento.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rentals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rental_date", nullable = false)
    private LocalDate rentalDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(name = "advance_paid", precision = 10, scale = 2)
    private BigDecimal advancePaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "damage_fee", precision = 10, scale = 2)
    private BigDecimal damageFee = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "advance_payment_done", nullable = false, columnDefinition = "boolean default false")
    private Boolean advancePaymentDone = false;

    @Builder.Default
    @Column(name = "balance_payment_done", nullable = false, columnDefinition = "boolean default false")
    private Boolean balancePaymentDone = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RentalItem> rentalItems;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = Status.PENDING;
        if (advancePaid == null) advancePaid = BigDecimal.ZERO;
        if (damageFee == null) damageFee = BigDecimal.ZERO;
        if (advancePaymentDone == null) advancePaymentDone = false;
        if (balancePaymentDone == null) balancePaymentDone = false;
    }

    public BigDecimal getBalanceDue() {
        if (totalAmount == null) return BigDecimal.ZERO;
        BigDecimal advance = advancePaid != null ? advancePaid : BigDecimal.ZERO;
        BigDecimal damage = damageFee != null ? damageFee : BigDecimal.ZERO;
        return totalAmount.subtract(advance).add(damage);
    }

    public BigDecimal getMinimumAdvance() {
        if (totalAmount == null) return BigDecimal.ZERO;
        return totalAmount.multiply(new BigDecimal("0.20"));
    }

    public enum Status {
        PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
    }
}
