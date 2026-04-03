package com.rento.integration.controller;

import com.rento.model.User;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User admin;
    private User targetUser;

    @BeforeEach
    void setUp() {
        // Create actual users in database that match @WithMockUser emails
        admin = userRepository.save(User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("pass"))
                .firstName("Admin").lastName("Test")
                .role(User.Role.ADMIN).build());

        userRepository.save(User.builder()
                .email("buyer@test.com")
                .password(passwordEncoder.encode("pass"))
                .firstName("Buyer").lastName("Test")
                .role(User.Role.BUYER).build());

        targetUser = userRepository.save(User.builder()
                .email("target@test.com")
                .password(passwordEncoder.encode("pass"))
                .firstName("Target").lastName("User")
                .role(User.Role.BUYER).build());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void usersPage_AsAdmin_Returns200() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    void usersPage_AsBuyer_Returns302() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void updateUserRole_Valid_Redirects() throws Exception {
        mockMvc.perform(post("/admin/users/" + targetUser.getId() + "/role").with(csrf())
                        .param("role", "SELLER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }
}