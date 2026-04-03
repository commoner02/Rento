package com.rento.controller;

import com.rento.dto.request.RentalRequest;
import com.rento.dto.request.ReturnRequest;
import com.rento.model.Rental;
import com.rento.model.User;
import com.rento.service.ProductService;
import com.rento.service.RentalService;
import com.rento.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public String listRentals(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.findByEmail(principal.getUsername());
        model.addAttribute("rentals", user.getRole() == User.Role.BUYER
                ? rentalService.findByUser(principal.getUsername())
                : rentalService.findAll());
        return "rentals/list";
    }

    @GetMapping("/{id}")
    public String viewRental(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal, Model model) {
        Rental rental = rentalService.findById(id);
        User user = userService.findByEmail(principal.getUsername());
        if (user.getRole() == User.Role.BUYER && !rental.getUser().getEmail().equals(principal.getUsername()))
            return "redirect:/error/403";
        model.addAttribute("rental", rental);
        model.addAttribute("payments", rentalService.findPaymentsByRental(id));
        model.addAttribute("currentUser", user);
        return "rentals/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('BUYER')")
    public String newRentalForm(Model model) {
        model.addAttribute("rentalRequest", new RentalRequest());
        model.addAttribute("products", productService.findAvailable());
        return "rentals/new";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('BUYER')")
    public String createRental(@Valid @ModelAttribute RentalRequest request, BindingResult result,
                               @AuthenticationPrincipal UserDetails principal,
                               Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("products", productService.findAvailable());
            return "rentals/new";
        }
        try {
            Rental rental = rentalService.createRental(request, principal.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "Rental created! Please pay the advance to proceed.");
            return "redirect:/rentals/" + rental.getId();
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("products", productService.findAvailable());
            return "rentals/new";
        }
    }

    @PostMapping("/{id}/pay-advance")
    @PreAuthorize("hasRole('BUYER')")
    public String payAdvance(@PathVariable Long id, @RequestParam BigDecimal amount,
                             RedirectAttributes ra) {
        try {
            rentalService.payAdvance(id, amount);
            ra.addFlashAttribute("successMsg", "Advance of Tk " + amount + " paid! (Demo)");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/pay-balance")
    @PreAuthorize("hasRole('BUYER')")
    public String payBalance(@PathVariable Long id, RedirectAttributes ra) {
        try {
            rentalService.payBalance(id);
            ra.addFlashAttribute("successMsg", "Balance paid! Rental fully settled. (Demo)");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public String confirmRental(@PathVariable Long id, RedirectAttributes ra) {
        try {
            rentalService.confirmRental(id);
            ra.addFlashAttribute("successMsg", "Rental confirmed.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public String activateRental(@PathVariable Long id, RedirectAttributes ra) {
        rentalService.activateRental(id);
        ra.addFlashAttribute("successMsg", "Rental is now active.");
        return "redirect:/rentals/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelRental(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails principal,
                               RedirectAttributes ra) {
        Rental rental = rentalService.findById(id);
        User user = userService.findByEmail(principal.getUsername());
        if (user.getRole() == User.Role.BUYER && !rental.getUser().getEmail().equals(principal.getUsername()))
            return "redirect:/error/403";
        rentalService.cancelRental(id);
        ra.addFlashAttribute("successMsg", "Rental cancelled.");
        return "redirect:/rentals";
    }

    @GetMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public String returnForm(@PathVariable Long id, Model model) {
        model.addAttribute("rental", rentalService.findById(id));
        model.addAttribute("returnRequest", new ReturnRequest());
        return "rentals/return";
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public String processReturn(@PathVariable Long id, @ModelAttribute ReturnRequest returnRequest,
                                RedirectAttributes ra) {
        rentalService.processReturn(id, returnRequest);
        ra.addFlashAttribute("successMsg", "Return processed. Buyer can now pay the balance.");
        return "redirect:/rentals/" + id;
    }
}
