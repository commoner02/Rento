package com.rento.integration.controller;

import com.rento.model.*;
import com.rento.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User buyer;
    private User seller;
    private Product product;
    private Rental pendingRental;

    @BeforeEach
    void setUp() {
        buyer = userRepository.save(User.builder()
                .email("buyer@test.com")
                .password(passwordEncoder.encode("pass"))
                .firstName("Buyer")
                .lastName("Test")
                .role(User.Role.BUYER)
                .build());

        seller = userRepository.save(User.builder()
                .email("seller@test.com")
                .password(passwordEncoder.encode("pass"))
                .firstName("Seller")
                .lastName("Test")
                .role(User.Role.SELLER)
                .build());

        product = productRepository.save(Product.builder()
                .name("Test Chair")
                .category(Product.Category.CHAIR)
                .dailyRate(new BigDecimal("2.50"))
                .totalQuantity(10)
                .availableQuantity(10)
                .replacementValue(new BigDecimal("25.00"))
                .build());

        // Create a pending rental for confirm tests
        pendingRental = rentalRepository.save(Rental.builder()
                .user(buyer)
                .rentalDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(3))
                .status(Rental.Status.PENDING)
                .totalAmount(new BigDecimal("30.00"))
                .advancePaid(new BigDecimal("10.00"))
                .advancePaymentDone(true)
                .balancePaymentDone(false)
                .damageFee(BigDecimal.ZERO)
                .build());
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    void getNewRentalForm_AsBuyer_Returns200() throws Exception {
        mockMvc.perform(get("/rentals/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("rentals/new"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    void createRental_ValidData_Redirects() throws Exception {
        mockMvc.perform(post("/rentals/new").with(csrf())
                        .param("rentalDate", LocalDate.now().toString())
                        .param("returnDate", LocalDate.now().plusDays(3).toString())
                        .param("items[0].productId", product.getId().toString())
                        .param("items[0].quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/rentals/*"));
    }

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    void confirmRental_AsSeller_Returns302() throws Exception {
        mockMvc.perform(post("/rentals/" + pendingRental.getId() + "/confirm").with(csrf()))
                .andExpect(status().is3xxRedirection()); // Redirects after success
    }
}