package com.chiton.api.service;

import com.chiton.api.entity.ProductionOrder;
import com.chiton.api.repository.ProductionOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductionOrderServiceImpl implements ProductionOrderService{

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Override
    public List<ProductionOrder> findAll() {
        return productionOrderRepository.findAll();
    }

    @Override
    public Optional<ProductionOrder> findById(Long id) {
        return productionOrderRepository.findById(id);
    }

    @Override
    public List<ProductionOrder> findAllByCustomerName(String customer) {
        return productionOrderRepository.findAllByCustomerName(customer);
    }

    @Override
    public ProductionOrder save(ProductionOrder productionOrder) {
        return productionOrderRepository.save(productionOrder);
    }
}
