package com.inventory.inventory.specification;

import com.inventory.inventory.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ProductSpecification {

    public static Specification<Product> hasCategory(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.isBlank()) {
                return cb.conjunction(); // no-op filter
            }
            return cb.equal(root.get("category").get("name"), categoryName);
        };
    }

    public static Specification<Product> hasMinQuantity(Integer minQty) {
        return (root, query, cb) -> {
            if (minQty == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("quantityInStock"), minQty);
        };
    }

    public static Specification<Product> hasMaxQuantity(Integer maxQty) {
        return (root, query, cb) -> {
            if (maxQty == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("quantityInStock"), maxQty);
        };
    }

    public static Specification<Product> createdAfter(LocalDateTime fromDate) {
        return (root, query, cb) -> {
            if (fromDate == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
        };
    }

    public static Specification<Product> createdBefore(LocalDateTime toDate) {
        return (root, query, cb) -> {
            if (toDate == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
        };
    }
}