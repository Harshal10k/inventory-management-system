package com.inventory.inventory.controller;

import com.inventory.inventory.dto.request.PurchaseOrderRequest;
import com.inventory.inventory.dto.request.PurchaseOrderStatusRequest;
import com.inventory.inventory.model.PurchaseOrder;
import com.inventory.inventory.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Autowired
    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAllOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> createOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrder created = purchaseOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PurchaseOrder> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderStatusRequest request) {
        PurchaseOrder updated = purchaseOrderService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }
}