package com.chiton.api.controller;

import com.chiton.api.entity.Category;
import com.chiton.api.entity.Product;
import com.chiton.api.entity.User;
import com.chiton.api.service.CategoryService;
import com.chiton.api.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;


    @GetMapping()
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody Category category) {
        Category existingCategory = categoryService.findByName(category.getName());
        if (existingCategory != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La categoría ya existe");
        }
        category.setStatus("Activado");
        Category newCategory = categoryService.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Category category) {
        Optional<Category> optionalCategory = categoryService.findById(id);
        if (optionalCategory.isPresent()) {
            category.setId(id);
            categoryService.save(category);
            return ResponseEntity.status(HttpStatus.OK).body(category);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria no encontrada con ID: " + id);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        Optional<Category> optionalCategory = categoryService.findById(id);

        if (optionalCategory.isPresent()) {
            Category category = optionalCategory.get();
            // Verificar si existen productos asociados a la categoría
            List<Product> productList = productService.findByCategoryName(category.getName());
            if (!productList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede eliminar la categoría porque tiene productos asociados.");
            }

            // No hay productos asociados, proceder con la eliminación de la categoría
            categoryService.deleteById(id);
            return ResponseEntity.ok("Categoría eliminada con ID: " + id);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoría no encontrada");
        }
    }
}