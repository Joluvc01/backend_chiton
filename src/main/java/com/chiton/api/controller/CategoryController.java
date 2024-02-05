package com.chiton.api.controller;

import com.chiton.api.dto.CategoryDTO;
import com.chiton.api.entity.Category;
import com.chiton.api.entity.Product;
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
    public CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ConvertDTO convertDTO;


    @GetMapping()
    public ResponseEntity<?> findAll() {
        List<Category> categories = categoryService.findAll();
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(convertDTO::convertToCategoryDTO)
                .toList();
        return ResponseEntity.ok(categoryDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(category -> ResponseEntity.ok(convertDTO.convertToCategoryDTO(category)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody CategoryDTO categoryDTO) {
        Category existingCategory = categoryService.findByName(categoryDTO.getName());
        if (existingCategory != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La categoría ya existe");
        }
        Category newcategory = new Category();
        newcategory.setName(categoryDTO.getName());
        newcategory.setStatus("Activado");
        categoryService.save(newcategory);
        CategoryDTO newCategoryDTO = convertDTO.convertToCategoryDTO(newcategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategoryDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        Optional<Category> optionalCategory = categoryService.findById(id);
        if(optionalCategory.isPresent()){
            Category existingCategory = optionalCategory.get();
            existingCategory.setName(categoryDTO.getName());
            Category updatedCategory = categoryService.save(existingCategory);
            CategoryDTO updatedCategoryDTO = convertDTO.convertToCategoryDTO(updatedCategory);
            return ResponseEntity.ok(updatedCategoryDTO);
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria no encontrado");
        }
    }

    @PostMapping("/status/{id}")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        Optional<Category> optionalCategory = categoryService.findById(id);

        if (optionalCategory.isPresent()) {
            Category existingCategory = optionalCategory.get();

            // Cambiar el estado de la categoría
            String currentStatus = existingCategory.getStatus();
            String newStatus = currentStatus.equals("Activado") ? "Desactivado" : "Activado";
            existingCategory.setStatus(newStatus);

            categoryService.save(existingCategory);
            String message = newStatus.equals("Activado") ? "Categoría activada" : "Categoría desactivada";
            return ResponseEntity.ok().body(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoría no encontrada");
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se puede eliminar la categoría porque tiene " + productList.size() + " productos asociados.");
            }

            // No hay productos asociados, proceder con la eliminación de la categoría
            categoryService.deleteById(id);
            return ResponseEntity.ok("Categoría eliminada con ID: " + id);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoría no encontrada");
        }
    }
}