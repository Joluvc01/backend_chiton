package com.chiton.api.repository;

import com.chiton.api.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder,Long> {
}
