package com.inventory.inventory.dto.request;

import com.inventory.inventory.model.enums.PurchaseOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderStatusRequest {

    @NotNull(message = "Status is required")
    private PurchaseOrderStatus status;
}