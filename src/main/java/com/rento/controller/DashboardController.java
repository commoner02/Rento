package com.rento.controller;

import com.rento.model.Rental;
import com.rento.model.User;
import com.rento.service.ProductService;
import com.rento.service.RentalService;
import com.rento.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final RentalService rentalService;
    private final ProductService productService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.findByEmail(principal.getUsername());
        model.addAttribute("user", user);

        if (user.getRole() == User.Role.ADMIN) {
            model.addAttribute("totalUsers", userService.findAll().size());
            model.addAttribute("totalProducts", productService.findAll().size());
            model.addAttribute("totalRentals", rentalService.findAll().size());
            model.addAttribute("totalRevenue", rentalService.getTotalRevenue());
            model.addAttribute("recentRentals", rentalService.findAll().stream().limit(5).toList());
            return "admin/dashboard";
        }

        if (user.getRole() == User.Role.SELLER) {
            model.addAttribute("pendingRentals", rentalService.findByStatus(Rental.Status.PENDING));
            model.addAttribute("activeRentals", rentalService.findByStatus(Rental.Status.ACTIVE));
            model.addAttribute("overdueRentals", rentalService.findOverdue());
            return "seller/dashboard";
        }

        // BUYER
        model.addAttribute("myRentals", rentalService.findByUser(principal.getUsername()));
        model.addAttribute("availableProducts", productService.findAvailable());
        return "buyer/dashboard";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
