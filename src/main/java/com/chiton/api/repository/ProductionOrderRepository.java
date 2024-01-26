package com.chiton.api.repository;

import com.chiton.api.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder,Long> {
    List<ProductionOrder> findAllByCustomerName(String customer);
}
