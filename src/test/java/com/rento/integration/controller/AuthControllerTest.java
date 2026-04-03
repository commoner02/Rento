package com.rento.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void loginPage_Returns200() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void register_ValidData_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/auth/register").with(csrf())
                        .param("email", "newuser@test.com")
                        .param("password", "password123")
                        .param("firstName", "New")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void register_DuplicateEmail_StaysOnRegisterPage() throws Exception {
        // First registration
        mockMvc.perform(post("/auth/register").with(csrf())
                .param("email", "dup@test.com")
                .param("password", "password123")
                .param("firstName", "First")
                .param("lastName", "User"));

        // Second registration with same email
        mockMvc.perform(post("/auth/register").with(csrf())
                        .param("email", "dup@test.com")
                        .param("password", "password123")
                        .param("firstName", "Second")
                        .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }
}