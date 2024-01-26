package com.chiton.api.service;

import com.chiton.api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    public List<Product> findAll();

    public Optional<Product> findById(Long id);

    Product findByNameAndColor(String name, String color);
    Product findByName(String name);

    List<Product> findByCategoryName(String category);

    public Product save(Product product);

    public void deleteById(Long id);
}
