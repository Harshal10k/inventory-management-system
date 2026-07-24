package com.inventory.inventory.service;

import com.inventory.inventory.dto.request.PurchaseOrderItemRequest;
import com.inventory.inventory.dto.request.PurchaseOrderRequest;
import com.inventory.inventory.exception.InvalidStockOperationException;
import com.inventory.inventory.exception.ResourceNotFoundException;
import com.inventory.inventory.model.*;
import com.inventory.inventory.model.enums.PurchaseOrderStatus;
import com.inventory.inventory.repository.ProductRepository;
import com.inventory.inventory.repository.PurchaseOrderRepository;
import com.inventory.inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Autowired
    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                 SupplierRepository supplierRepository,
                                 ProductRepository productRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    public List<PurchaseOrder> getAllOrders() {
        return purchaseOrderRepository.findAll();
    }

    public PurchaseOrder getOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    @Transactional
    public PurchaseOrder createOrder(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with id: " + request.getSupplierId()));

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setStatus(PurchaseOrderStatus.PENDING);

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemReq.getProductId()));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitCost(itemReq.getUnitCost());

            order.addItem(item); // keeps both sides of the relationship in sync
        }

        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public PurchaseOrder updateStatus(Long id, PurchaseOrderStatus newStatus) {
        PurchaseOrder order = getOrderById(id);
        PurchaseOrderStatus currentStatus = order.getStatus();

        if (currentStatus == PurchaseOrderStatus.RECEIVED || currentStatus == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidStockOperationException(
                    "Cannot change status of an order that is already " + currentStatus);
        }

        if (newStatus == PurchaseOrderStatus.RECEIVED) {
            // auto-increment stock for every line item
            for (PurchaseOrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        return purchaseOrderRepository.save(order);
    }
}