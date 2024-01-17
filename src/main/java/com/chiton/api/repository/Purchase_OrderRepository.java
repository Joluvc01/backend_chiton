package com.chiton.api.repository;

import com.chiton.api.entity.Purchase_order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Purchase_OrderRepository extends JpaRepository<Purchase_order,Long> {
}
