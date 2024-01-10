package com.chiton.api.service;

import com.chiton.api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

public interface ProductService {
    public Page<Product> findAll(Pageable pageable);

    public Optional<Product> findById(Long id);

    public Page<Product> findByCategoryName(String categoryName, Pageable pageable);

    public Product save(Product product);

    public void deleteById(Long id);

}
