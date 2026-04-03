package com.rento.unit.security;

import com.rento.controller.RentalController;
import com.rento.service.ProductService;
import com.rento.service.RentalService;
import com.rento.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RentalController.class)
public class RentalControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RentalService rentalService;

    @MockBean
    private UserService userService;

    @MockBean
    private ProductService productService;

    @Test
    @WithMockUser(roles = "SELLER")
    void getNewRentalForm_AsSeller_ShouldAllowAccess() throws Exception {
        mockMvc.perform(get("/rentals/new"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void payAdvance_AsSeller_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/rentals/1/pay-advance")
                        .param("amount", "15.00")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void confirmRental_AsBuyer_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/rentals/1/confirm")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}