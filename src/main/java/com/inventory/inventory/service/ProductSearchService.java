package com.inventory.inventory.service;

import com.inventory.inventory.dto.ParsedProductFilter;
import com.inventory.inventory.model.Product;
import com.inventory.inventory.specification.ProductSpecification;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {

    private final ChatClient chatClient;
    private final ProductService productService;

    private static final String SYSTEM_PROMPT = """
            You convert a warehouse staff member's natural-language product search into structured filter fields.
            Extract only what is explicitly stated or clearly implied in the query. Leave fields null if not mentioned.
            - category: the product category name mentioned, if any (e.g. "Electronics", "Furniture")
            - minQty: minimum stock quantity, if a lower bound is mentioned
            - maxQty: maximum stock quantity, if an upper bound is mentioned
            - maxPrice: maximum unit price, if a price ceiling is mentioned
            - lowStockOnly: true if the user is asking specifically about low-stock or running-low items, otherwise null
            Respond with ONLY the structured data, no explanation.
            """;

    @Autowired
    public ProductSearchService(ChatClient.Builder chatClientBuilder, ProductService productService) {
        this.chatClient = chatClientBuilder.build();
        this.productService = productService;
    }
    
    public List<Product> searchByNaturalLanguage(String query) {
        ParsedProductFilter filter = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(query)
                .call()
                .entity(ParsedProductFilter.class);

        return productService.filterProducts(
                filter.category(), filter.minQty(), filter.maxQty(),
                null, null, filter.maxPrice(), filter.lowStockOnly()
        );
    }

    
}