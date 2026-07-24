package com.inventory.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.inventory.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
