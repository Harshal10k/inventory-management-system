package com.inventory.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier id is required")
    private Long supplierId;

    @NotEmpty(message = "At least one line item is required")
    @Valid
    private List<PurchaseOrderItemRequest> items;
}