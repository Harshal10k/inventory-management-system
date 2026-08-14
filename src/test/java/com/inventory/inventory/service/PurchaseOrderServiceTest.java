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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private Supplier supplier;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("TechSupply Co.");

        product1 = new Product();
        product1.setId(1L);
        product1.setName("Wireless Mouse");
        product1.setQuantityInStock(50);
        product1.setReorderThreshold(10);

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Keyboard");
        product2.setQuantityInStock(30);
        product2.setReorderThreshold(5);
    }

    @Test
    void createOrder_shouldCreateOrderWithNestedItems() {
        PurchaseOrderItemRequest itemReq1 = new PurchaseOrderItemRequest();
        itemReq1.setProductId(1L);
        itemReq1.setQuantity(20);
        itemReq1.setUnitCost(350.0);

        PurchaseOrderItemRequest itemReq2 = new PurchaseOrderItemRequest();
        itemReq2.setProductId(2L);
        itemReq2.setQuantity(10);
        itemReq2.setUnitCost(120.0);

        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(1L);
        request.setItems(Arrays.asList(itemReq1, itemReq2));

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = purchaseOrderService.createOrder(request);

        assertThat(result.getSupplier()).isEqualTo(supplier);
        assertThat(result.getStatus()).isEqualTo(PurchaseOrderStatus.PENDING);
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getProduct()).isEqualTo(product1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(20);
        // stock should NOT change at creation time — only on RECEIVED
        assertThat(product1.getQuantityInStock()).isEqualTo(50);
    }

    @Test
    void createOrder_shouldThrow_whenSupplierNotFound() {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(99L);
        request.setItems(List.of());

        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseOrderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with id: 99");

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldThrow_whenProductInLineItemNotFound() {
        PurchaseOrderItemRequest itemReq = new PurchaseOrderItemRequest();
        itemReq.setProductId(99L);
        itemReq.setQuantity(5);
        itemReq.setUnitCost(100.0);

        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(1L);
        request.setItems(List.of(itemReq));

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseOrderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldIncrementStock_whenMarkedReceived() {
        PurchaseOrder order = buildPendingOrderWithItems();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = purchaseOrderService.updateStatus(1L, PurchaseOrderStatus.RECEIVED);

        assertThat(result.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        assertThat(product1.getQuantityInStock()).isEqualTo(70); // 50 + 20
        assertThat(product2.getQuantityInStock()).isEqualTo(40); // 30 + 10
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void updateStatus_shouldNotChangeStock_whenMarkedCancelled() {
        PurchaseOrder order = buildPendingOrderWithItems();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = purchaseOrderService.updateStatus(1L, PurchaseOrderStatus.CANCELLED);

        assertThat(result.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        assertThat(product1.getQuantityInStock()).isEqualTo(50); // unchanged
        assertThat(product2.getQuantityInStock()).isEqualTo(30); // unchanged
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldThrow_whenOrderAlreadyReceived() {
        PurchaseOrder order = buildPendingOrderWithItems();
        order.setStatus(PurchaseOrderStatus.RECEIVED);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.updateStatus(1L, PurchaseOrderStatus.CANCELLED))
                .isInstanceOf(InvalidStockOperationException.class)
                .hasMessageContaining("already RECEIVED");

        verify(purchaseOrderRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldThrow_whenOrderAlreadyCancelled() {
        PurchaseOrder order = buildPendingOrderWithItems();
        order.setStatus(PurchaseOrderStatus.CANCELLED);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.updateStatus(1L, PurchaseOrderStatus.RECEIVED))
                .isInstanceOf(InvalidStockOperationException.class)
                .hasMessageContaining("already CANCELLED");
    }

    @Test
    void updateStatus_shouldThrow_whenOrderNotFound() {
        when(purchaseOrderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseOrderService.updateStatus(404L, PurchaseOrderStatus.RECEIVED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // Helper to build a PENDING order with two line items, mirroring the createOrder flow
    private PurchaseOrder buildPendingOrderWithItems() {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setSupplier(supplier);
        order.setStatus(PurchaseOrderStatus.PENDING);

        PurchaseOrderItem item1 = new PurchaseOrderItem();
        item1.setProduct(product1);
        item1.setQuantity(20);
        item1.setUnitCost(350.0);

        PurchaseOrderItem item2 = new PurchaseOrderItem();
        item2.setProduct(product2);
        item2.setQuantity(10);
        item2.setUnitCost(120.0);

        order.addItem(item1);
        order.addItem(item2);

        return order;
    }
}