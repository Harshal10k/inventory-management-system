package com.inventory.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaturalLanguageSearchRequest {

    @NotBlank(message = "Query text is required")
    private String query;
}