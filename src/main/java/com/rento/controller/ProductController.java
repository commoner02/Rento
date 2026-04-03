package com.rento.controller;

import com.rento.dto.request.ProductRequest;
import com.rento.model.Product;
import com.rento.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(Model model,
                               @RequestParam(required = false) Product.Category category) {
        if (category != null) {
            model.addAttribute("products", productService.findByCategory(category));
            model.addAttribute("selectedCategory", category);
        } else {
            model.addAttribute("products", productService.findAll());
        }
        model.addAttribute("categories", Product.Category.values());
        return "products/list";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "products/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newProductForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", Product.Category.values());
        model.addAttribute("isEdit", false);
        return "products/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createProduct(@Valid @ModelAttribute ProductRequest request,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Product.Category.values());
            model.addAttribute("isEdit", false);
            return "products/form";
        }
        productService.create(request);
        redirectAttributes.addFlashAttribute("successMsg", "Product created successfully.");
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setCategory(product.getCategory());
        request.setDailyRate(product.getDailyRate());
        request.setTotalQuantity(product.getTotalQuantity());
        request.setAvailableQuantity(product.getAvailableQuantity());
        request.setReplacementValue(product.getReplacementValue());
        request.setImageUrl(product.getImageUrl());

        model.addAttribute("productRequest", request);
        model.addAttribute("productId", id);
        model.addAttribute("categories", Product.Category.values());
        model.addAttribute("isEdit", true);
        return "products/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute ProductRequest request,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Product.Category.values());
            model.addAttribute("productId", id);
            model.addAttribute("isEdit", true);
            return "products/form";
        }
        productService.update(id, request);
        redirectAttributes.addFlashAttribute("successMsg", "Product updated successfully.");
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMsg", "Product deleted.");
        return "redirect:/products";
    }
}
