package com.inventory.inventory.dto;

public record ParsedProductFilter(
        String category,
        Integer minQty,
        Integer maxQty,
        Double maxPrice,
        Boolean lowStockOnly
) {}