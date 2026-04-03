package com.rento.unit.service;

import com.rento.dto.request.ProductRequest;
import com.rento.exception.ResourceNotFoundException;
import com.rento.model.Product;
import com.rento.repository.ProductRepository;
import com.rento.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    private Product chair;
    private ProductRequest chairRequest;

    @BeforeEach
    void setUp() {
        chair = Product.builder()
                .id(1L)
                .name("Folding Chair")
                .category(Product.Category.CHAIR)
                .dailyRate(new BigDecimal("2.50"))
                .totalQuantity(100)
                .availableQuantity(50)
                .replacementValue(new BigDecimal("25.00"))
                .build();

        chairRequest = new ProductRequest();
        chairRequest.setName("Folding Chair");
        chairRequest.setCategory(Product.Category.CHAIR);
        chairRequest.setDailyRate(new BigDecimal("2.50"));
        chairRequest.setTotalQuantity(100);
        chairRequest.setAvailableQuantity(50);
        chairRequest.setReplacementValue(new BigDecimal("25.00"));
    }

    @Test
    void create_ValidRequest_SavesAndReturnsProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(chair);

        Product result = productService.create(chairRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Folding Chair");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void findById_ExistingId_ReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(chair));

        Product result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_NonExistentId_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_ExistingProduct_UpdatesFields() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(chair));
        when(productRepository.save(any())).thenReturn(chair);

        chairRequest.setName("Premium Chair");
        Product result = productService.update(1L, chairRequest);

        assertThat(result.getName()).isEqualTo("Premium Chair");
        verify(productRepository).save(chair);
    }

    @Test
    void isAvailable_EnoughStock_ReturnsTrue() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(chair));

        assertThat(productService.isAvailable(1L, 50)).isTrue();
        assertThat(productService.isAvailable(1L, 51)).isFalse();
    }
}