package com.rento.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rental_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RentalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "daily_rate_at_rental", precision = 10, scale = 2)
    private BigDecimal dailyRateAtRental;

    // Simplified return tracking
    @Builder.Default
    @Column(name = "returned_quantity")
    private Integer returnedQuantity = 0;

    @Column(name = "condition_notes")
    private String conditionNotes;

    @Builder.Default
    @Column(name = "damage_fee", precision = 10, scale = 2)
    private BigDecimal damageFee = BigDecimal.ZERO;
}
