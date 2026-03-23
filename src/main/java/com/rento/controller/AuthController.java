package com.rento.controller;

import com.rento.dto.request.RegisterRequest;
import com.rento.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute("successMsg", "Account created! Please log in.");
            return "redirect:/auth/login";
        } catch (IllegalStateException e) {
            result.rejectValue("email", "error.email", e.getMessage());
            return "auth/register";
        }
    }
}
