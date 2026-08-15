package com.inventory.inventory.service;

import com.inventory.inventory.exception.InvalidStockOperationException;
import com.inventory.inventory.exception.ResourceNotFoundException;
import com.inventory.inventory.model.Supplier;
import com.inventory.inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
        		.orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {
        Supplier existing = getSupplierById(id);
        existing.setName(updatedSupplier.getName());
        existing.setContactEmail(updatedSupplier.getContactEmail());
        existing.setContactPhone(updatedSupplier.getContactPhone());
        existing.setAddress(updatedSupplier.getAddress());
        return supplierRepository.save(existing);
    }

    public void deleteSupplier(Long id) {
        Supplier existing = getSupplierById(id);
        try {
            supplierRepository.delete(existing);
        } catch (DataIntegrityViolationException ex) {
            throw new InvalidStockOperationException(
                "Cannot delete supplier '" + existing.getName() + "' — it has existing purchase orders. " +
                "Remove or reassign those orders first."
            );
        }
    }
}