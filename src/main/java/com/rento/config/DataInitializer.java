package com.rento.config;

import com.rento.model.Product;
import com.rento.model.User;
import com.rento.repository.ProductRepository;
import com.rento.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return; // skip if already seeded

        log.info("Seeding initial data...");

        // Users
        userRepository.save(User.builder()
                .email("admin@rento.com").password(passwordEncoder.encode("admin123"))
                .firstName("Admin").lastName("User").role(User.Role.ADMIN).build());

        userRepository.save(User.builder()
                .email("seller@rento.com").password(passwordEncoder.encode("seller123"))
                .firstName("Sarah").lastName("Seller").phone("01711-000001").role(User.Role.SELLER).build());

        userRepository.save(User.builder()
                .email("buyer@rento.com").password(passwordEncoder.encode("buyer123"))
                .firstName("Ben").lastName("Buyer").phone("01711-000002").role(User.Role.BUYER).build());

        // Products
        productRepository.save(Product.builder()
                .name("Folding Chair").description("Sturdy white folding chair, perfect for events.")
                .category(Product.Category.CHAIR).dailyRate(new BigDecimal("2.50"))
                .totalQuantity(100).availableQuantity(100).replacementValue(new BigDecimal("25.00")).build());

        productRepository.save(Product.builder()
                .name("6ft Folding Table").description("Heavy-duty 6 foot folding table.")
                .category(Product.Category.TABLE).dailyRate(new BigDecimal("15.00"))
                .totalQuantity(30).availableQuantity(30).replacementValue(new BigDecimal("120.00")).build());

        productRepository.save(Product.builder()
                .name("10x10 Pop-up Canopy").description("Weather-resistant pop-up canopy tent.")
                .category(Product.Category.CANOPY).dailyRate(new BigDecimal("45.00"))
                .totalQuantity(10).availableQuantity(10).replacementValue(new BigDecimal("350.00")).build());

        productRepository.save(Product.builder()
                .name("White Table Linen 6ft").description("Clean white linen for 6ft tables.")
                .category(Product.Category.LINEN).dailyRate(new BigDecimal("8.00"))
                .totalQuantity(50).availableQuantity(50).replacementValue(new BigDecimal("25.00")).build());

        productRepository.save(Product.builder()
                .name("String Light Set 50ft").description("Warm white string lights for ambiance.")
                .category(Product.Category.LIGHTING).dailyRate(new BigDecimal("12.00"))
                .totalQuantity(20).availableQuantity(20).replacementValue(new BigDecimal("60.00")).build());

        productRepository.save(Product.builder()
                .name("Bluetooth Speaker").description("Portable outdoor Bluetooth speaker.")
                .category(Product.Category.AUDIO).dailyRate(new BigDecimal("25.00"))
                .totalQuantity(8).availableQuantity(8).replacementValue(new BigDecimal("180.00")).build());

        log.info("Data seeding complete.");
    }
}
