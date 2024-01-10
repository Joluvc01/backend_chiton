package com.chiton.api.service;

import com.chiton.api.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    public List<Category> findAll();

    public Optional<Category> findById(Long id);

    public Category save(Category category);

    public void deleteById(Long id);
}
