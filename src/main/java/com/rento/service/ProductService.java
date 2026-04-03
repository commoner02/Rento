package com.rento.service;

import com.rento.dto.request.ProductRequest;
import com.rento.exception.ResourceNotFoundException;
import com.rento.model.Product;
import com.rento.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .dailyRate(request.getDailyRate())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getAvailableQuantity())
                .replacementValue(request.getReplacementValue())
                .imageUrl(request.getImageUrl())
                .build();
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> findAvailable() {
        return productRepository.findByAvailableQuantityGreaterThan(0);
    }

    @Transactional(readOnly = true)
    public List<Product> findByCategory(Product.Category category) {
        return productRepository.findByCategory(category);
    }

    public Product update(Long id, ProductRequest request) {
        Product product = findById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setDailyRate(request.getDailyRate());
        product.setTotalQuantity(request.getTotalQuantity());
        product.setAvailableQuantity(request.getAvailableQuantity());
        product.setReplacementValue(request.getReplacementValue());
        product.setImageUrl(request.getImageUrl());
        return productRepository.save(product);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public boolean isAvailable(Long productId, int requestedQuantity) {
        Product product = findById(productId);
        return product.getAvailableQuantity() >= requestedQuantity;
    }
}
