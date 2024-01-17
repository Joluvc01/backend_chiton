package com.chiton.api.repository;

import com.chiton.api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategoryName(String categoryName, Pageable pageable);
    Product findByNameAndColor(String name, String color);
}
