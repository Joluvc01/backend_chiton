package com.chiton.api.controller;

import com.chiton.api.dto.CategoryDTO;
import com.chiton.api.entity.Category;
import com.chiton.api.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CategoryDTO categoryDTO) {
        // Verificar si la categoría ya existe
        Category existingCategory = categoryService.findByName(categoryDTO.getName());

        if (existingCategory != null) {
            // Si la categoría ya existe, devolver un mensaje indicando el conflicto
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La categoría ya existe");
        }

        // Convierte el CategoryDTO a Category
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setStatus(categoryDTO.getStatus());

        // Guarda la categoría en la base de datos
        Category savedCategory = categoryService.save(category);

        // Devuelve la respuesta con la categoría creada
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }


    @PutMapping("/update/{categoryId}")
    public ResponseEntity<?> update(@PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO) {
        // Verifica si la categoria con el ID especificado existe en la base de datos
        Optional<Category> optionalCategory = categoryService.findById(categoryId);

        if (optionalCategory.isPresent()) {
            Category existingCategory = optionalCategory.get();

            // Actualiza los atributos de la categoria con los valores del DTO
            existingCategory.setName(categoryDTO.getName());
            existingCategory.setStatus(categoryDTO.getStatus());

            // Guarda la categoria actualizada en la base de datos
            Category updatedCategory = categoryService.save(existingCategory);

            // Devuelve la respuesta con la categoria actualizada
            return ResponseEntity.ok(updatedCategory);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria no encontrada con ID: " + categoryId);
        }
    }

    @DeleteMapping("/delete/{categoryId}")
    public ResponseEntity<?> delete(@PathVariable Long categoryId) {
        // Verifica si la categoria con el ID especificado existe en la base de datos
        Optional<Category> optionalCategory = categoryService.findById(categoryId);

        if (optionalCategory.isPresent()) {
            // Elimina la categoria con el ID especificado
            categoryService.deleteById(categoryId);

            // Devuelve la respuesta con el mensaje de eliminación exitosa
            return ResponseEntity.ok("Categoria eliminada con ID: " + categoryId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria no encontrada con ID: " + categoryId);
        }
    }
}