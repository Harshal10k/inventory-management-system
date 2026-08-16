package com.inventory.inventory.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.inventory.inventory.exception.InvalidStockOperationException;
import com.inventory.inventory.exception.ResourceNotFoundException;
import com.inventory.inventory.model.Product;
import com.inventory.inventory.repository.ProductRepository;
import com.inventory.inventory.specification.ProductSpecification;


@Service
public class ProductService {
	
	private final ProductRepository productRepository;
	
	@Autowired
	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}
	
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}
	
	public Product getProductById(Long id) {
		return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
	}
	
	public Product createProduct(Product product) {
		return productRepository.save(product);
	}
	
	public Product updateProduct(Long id, Product updatedProduct) {
		Product existing = getProductById(id);
		existing.setName(updatedProduct.getName());
		existing.setSku(updatedProduct.getSku());
		existing.setCategory(updatedProduct.getCategory());
        existing.setUnitPrice(updatedProduct.getUnitPrice());
        existing.setQuantityInStock(updatedProduct.getQuantityInStock());
        existing.setReorderThreshold(updatedProduct.getReorderThreshold());
		return productRepository.save(existing);
	}
	
	public void deleteProduct(Long id) {
		Product existing = getProductById(id);
		productRepository.delete(existing);
	}
	
	public List<Product> filterProducts(String category, Integer minQty, Integer maxQty, LocalDateTime fromDate, LocalDateTime toDate, Double maxPrice, Boolean lowStockOnly) {
		Specification<Product> spec = Specification
				.where(ProductSpecification.hasCategory(category))
				.and(ProductSpecification.hasMinQuantity(minQty))
				.and(ProductSpecification.hasMaxQuantity(maxQty))
				.and(ProductSpecification.createdAfter(fromDate))
				.and(ProductSpecification.createdBefore(toDate))
				.and(ProductSpecification.hasMaxPrice(maxPrice))
				.and(ProductSpecification.isLowStock(lowStockOnly));

		return productRepository.findAll(spec);
	}
	
	public Product adjustStock(Long id, Integer quantityChange) {
	    Product product = getProductById(id);
	    int newQuantity = product.getQuantityInStock() + quantityChange;

	    if (newQuantity < 0) {
	    	throw new InvalidStockOperationException(
	    		    "Cannot reduce stock below zero. Current: " + product.getQuantityInStock() + ", attempted change: " + quantityChange
	    		);
	    }

	    product.setQuantityInStock(newQuantity);
	    return productRepository.save(product);
	}
}
