package com.rento.integration.controller;

import com.rento.model.Product;
import com.rento.model.User;
import com.rento.repository.ProductRepository;
import com.rento.repository.UserRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        // Create actual users in database
        userRepository.save(User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("pass"))
                .firstName("Admin").lastName("Test")
                .role(User.Role.ADMIN).build());

        userRepository.save(User.builder()
                .email("buyer@test.com")
                .password(passwordEncoder.encode("pass"))
                .firstName("Buyer").lastName("Test")
                .role(User.Role.BUYER).build());

        savedProduct = productRepository.save(Product.builder()
                .name("Test Chair")
                .category(Product.Category.CHAIR)
                .dailyRate(new BigDecimal("2.50"))
                .totalQuantity(10)
                .availableQuantity(10)
                .build());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void listProducts_AsAdmin_Returns200() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void createProduct_ValidData_Redirects() throws Exception {
        mockMvc.perform(post("/products/new").with(csrf())
                        .param("name", "New Table")
                        .param("category", "TABLE")
                        .param("dailyRate", "15.00")
                        .param("totalQuantity", "10")
                        .param("availableQuantity", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    void createProduct_AsBuyer_Returns302() throws Exception {
        mockMvc.perform(post("/products/new").with(csrf())
                        .param("name", "Sneaky Chair")
                        .param("category", "CHAIR")
                        .param("dailyRate", "5.00")
                        .param("totalQuantity", "5")
                        .param("availableQuantity", "5"))
                .andExpect(status().is3xxRedirection());
    }
}