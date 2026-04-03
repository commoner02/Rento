package com.rento.controller;

import com.rento.model.User;
import com.rento.service.UserService;
import com.rento.service.RentalService;
import com.rento.model.Rental;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final RentalService rentalService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", User.Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String updateRole(@PathVariable Long id,
                             @RequestParam User.Role role,
                             RedirectAttributes redirectAttributes) {
        userService.updateRole(id, role);
        redirectAttributes.addFlashAttribute("successMsg", "User role updated.");
        return "redirect:/admin/users";
    }

    @GetMapping("/payments")
    public String revenue(Model model) {
        model.addAttribute("completedRentals", rentalService.findByStatus(Rental.Status.COMPLETED));
        model.addAttribute("totalRevenue", rentalService.getTotalRevenue());
        return "admin/payments";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMsg", "User deleted.");
        return "redirect:/admin/users";
    }
}
