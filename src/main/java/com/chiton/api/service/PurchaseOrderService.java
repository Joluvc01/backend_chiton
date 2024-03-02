package com.chiton.api.service;

import com.chiton.api.entity.PurchaseOrder;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderService {

    public List<PurchaseOrder> findAll();

    public Optional<PurchaseOrder> findById(Long id);

    public PurchaseOrder save(PurchaseOrder purchaseOrder);

    public void deleteById(Long id);
}
