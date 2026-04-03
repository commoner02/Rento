package com.rento.integration.repository;

import com.rento.model.Product;
import com.rento.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private TestEntityManager em;

    @BeforeEach
    void setUp() {
        em.persist(Product.builder()
                .name("Chair").category(Product.Category.CHAIR)
                .dailyRate(new BigDecimal("2.50")).totalQuantity(10)
                .availableQuantity(5).build());
        em.persist(Product.builder()
                .name("Table").category(Product.Category.TABLE)
                .dailyRate(new BigDecimal("15.00")).totalQuantity(5)
                .availableQuantity(3).build());
        em.flush();
    }

    @Test
    void findByCategory_ReturnsOnlyMatchingCategory() {
        List<Product> chairs = productRepository.findByCategory(Product.Category.CHAIR);
        assertThat(chairs).hasSize(1);
        assertThat(chairs.get(0).getCategory()).isEqualTo(Product.Category.CHAIR);
    }

    @Test
    void findByAvailableQuantityGreaterThan_ReturnsOnlyAvailable() {
        List<Product> available = productRepository.findByAvailableQuantityGreaterThan(0);
        assertThat(available).hasSize(2);
        assertThat(available).allMatch(p -> p.getAvailableQuantity() > 0);
    }
}