package com.inventory.inventory.service;


import com.inventory.inventory.exception.InvalidStockOperationException;
import com.inventory.inventory.exception.ResourceNotFoundException;
import com.inventory.inventory.model.Product;
import com.inventory.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Wireless Mouse");
        product.setSku("WM-001");
        product.setUnitPrice(499.0);
        product.setQuantityInStock(50);
        product.setReorderThreshold(10);
    }

    @Test
    void adjustStock_shouldIncreaseQuantity_whenPositiveDelta() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.adjustStock(1L, 20);

        assertThat(result.getQuantityInStock()).isEqualTo(70);
        verify(productRepository).save(product);
    }

    @Test
    void adjustStock_shouldDecreaseQuantity_whenNegativeDelta() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.adjustStock(1L, -30);

        assertThat(result.getQuantityInStock()).isEqualTo(20);
    }

    @Test
    void adjustStock_shouldThrow_whenResultingStockWouldBeNegative() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.adjustStock(1L, -100))
                .isInstanceOf(InvalidStockOperationException.class)
                .hasMessageContaining("Cannot reduce stock below zero");

        verify(productRepository, never()).save(any());
    }

    @Test
    void adjustStock_shouldThrow_whenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.adjustStock(99L, 10))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
    }

    @Test
    void getProductById_shouldReturnProduct_whenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertThat(result.getName()).isEqualTo("Wireless Mouse");
    }

    @Test
    void getProductById_shouldThrow_whenNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}