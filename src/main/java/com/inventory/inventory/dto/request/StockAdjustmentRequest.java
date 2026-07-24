package com.inventory.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockAdjustmentRequest {

    @NotNull(message = "Adjustment quantity is required")
    private Integer quantityChange; // positive to add stock, negative to remove

    private String reason; // optional, e.g. "restock", "damaged", "sold"
}