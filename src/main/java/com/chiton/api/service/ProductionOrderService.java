package com.chiton.api.service;

import com.chiton.api.entity.ProductionOrder;
import java.util.List;
import java.util.Optional;

public interface ProductionOrderService {

    public List<ProductionOrder> findAll();

    public Optional<ProductionOrder> findById(Long id);

    public ProductionOrder save(ProductionOrder productionOrder);

    public void deleteById(Long id);
}
