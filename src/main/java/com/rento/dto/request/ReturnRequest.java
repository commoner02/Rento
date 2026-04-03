package com.rento.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReturnRequest {
    private List<ReturnItemRequest> items;

    @Data
    public static class ReturnItemRequest {
        private Long rentalItemId;
        private Integer returnedQuantity;
        private String conditionNotes;
        private BigDecimal damageFee;
    }
}
