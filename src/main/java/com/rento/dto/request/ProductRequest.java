package com.rento.dto.request;

import com.rento.model.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Product.Category category;

    @NotNull @DecimalMin("0.01")
    private BigDecimal dailyRate;

    @NotNull @Min(1)
    private Integer totalQuantity;

    @NotNull @Min(1)
    private Integer availableQuantity;

    private BigDecimal replacementValue;
    private String imageUrl;
}
