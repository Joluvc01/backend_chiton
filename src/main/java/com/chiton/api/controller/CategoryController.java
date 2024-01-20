package com.chiton.api.controller;

import com.chiton.api.entity.Category;
import com.chiton.api.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(category);
        } else {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria no encontrada");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Category category) {
        Category existingCategory = categoryService.findByName(category.getName());
        if (existingCategory != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La categoría ya existe");
        }
        Category newCategory = categoryService.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Category category) {
        Optional<Category> optionalCategory = categoryService.findById(id);
        if (optionalCategory.isPresent()) {
            Category existingCategory = categoryService.findByName(category.getName());
            if (existingCategory != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Nombre de la categoría en uso");
            } else {
                category.setId(id);
                categoryService.save(category);
                return ResponseEntity.status(HttpStatus.OK).body(category);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria no encontrada con ID: " + id);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<Category> optionalCategory = categoryService.findById(id);
        if (optionalCategory.isPresent()) {
            categoryService.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Categoria eliminada con ID: " + id);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria no encontrada con ID: " + id);
        }
    }
}