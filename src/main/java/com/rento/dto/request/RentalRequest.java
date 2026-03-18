package com.rento.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RentalRequest {
    @NotNull
    private LocalDate rentalDate;

    @NotNull @Future
    private LocalDate returnDate;

    private String notes;

    @NotEmpty
    private List<RentalItemRequest> items;

    @Data
    public static class RentalItemRequest {
        @NotNull
        private Long productId;

        @NotNull
        private Integer quantity;
    }
}
