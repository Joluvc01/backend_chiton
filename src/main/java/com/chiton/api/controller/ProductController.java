package com.chiton.api.controller;

import com.chiton.api.dto.ProductDTO;
import com.chiton.api.entity.Category;
import com.chiton.api.entity.Product;
import com.chiton.api.service.CategoryService;
import com.chiton.api.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<Page<ProductDTO>> findByCategoryName(@RequestParam(required = false) String categoryName, Pageable pageable) {

        Page<Product> productsPage;

        if (categoryName != null && !categoryName.isEmpty()) {
            productsPage = productService.findByCategoryName(categoryName, pageable);
        } else {
            productsPage = productService.findAll(pageable);
        }

        // Mapear Product a ProductDTO con nombre de categoría
        Page<ProductDTO> productDTOs = productsPage.map(product -> {
            String category = product.getCategory() != null ? product.getCategory().getName() : null;
            return new ProductDTO(
                    product.getId(),
                    product.getName(),
                    product.getColor(),
                    product.getStock(),
                    category,
                    product.getStatus()
            );
        });

        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrUpdate(@RequestBody ProductDTO productDTO) {
        // Buscar un producto existente con los mismos datos
        Product existingProduct = productService.findByNameAndColor(productDTO.getName(), productDTO.getColor());

        if (existingProduct != null) {
            // Si el producto ya existe, actualiza el stock sumando la cantidad proporcionada
            existingProduct.setStock(existingProduct.getStock() + productDTO.getStock());

            // Guarda el producto actualizado en la base de datos
            Product updatedProduct = productService.save(existingProduct);

            // Devuelve la respuesta con el producto actualizado
            return ResponseEntity.ok(updatedProduct);
        } else {
            // Convierte ProductDTO a Product
            Product newProduct = new Product();
            newProduct.setName(productDTO.getName());
            newProduct.setColor(productDTO.getColor());
            newProduct.setStock(productDTO.getStock());
            newProduct.setStatus(productDTO.getStatus());

            // Buscar la categoría por nombre en la base de datos
            Category category = categoryService.findByName(productDTO.getCategory());

            if (category == null) {
                // Manejar el caso en el que la categoría no existe
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La categoría no existe");
            }

            newProduct.setCategory(category);

            // Guarda el nuevo producto en la base de datos
            productService.save(newProduct);

            // Devuelve la respuesta con el nuevo producto creado
            return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
        }
    }


    @PutMapping("/update/{productId}")
    public ResponseEntity<?> update(@PathVariable Long productId, @RequestBody ProductDTO productDTO) {
        // Verifica si el producto con el ID especificado existe en la base de datos
        Optional<Product> optionalProduct = productService.findById(productId);

        if (optionalProduct.isPresent()) {
            Product existingProduct = optionalProduct.get();

            // Actualiza los atributos del producto con los valores del DTO
            existingProduct.setName(productDTO.getName());
            existingProduct.setColor(productDTO.getColor());
            existingProduct.setStock(productDTO.getStock());

            // Busca la categoría por nombre
            Category category = categoryService.findByName(productDTO.getCategory());

            if (category != null) {
                // Establece la categoría en el producto
                existingProduct.setCategory(category);

                // Guarda el producto actualizado en la base de datos
                Product updatedProduct = productService.save(existingProduct);

                // Devuelve la respuesta con el producto actualizado
                return ResponseEntity.ok(updatedProduct);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoría no encontrada: " + productDTO.getCategory());
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado con ID: " + productId);
        }
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<?> deleteProducto(@PathVariable Long productId) {
        // Verifica si la categoria con el ID especificado existe en la base de datos
        Optional<Product> optionalProduct = productService.findById(productId);

        if (optionalProduct.isPresent()) {
            // Elimina la categoria con el ID especificado
            productService.deleteById(productId);

            // Devuelve la respuesta con el mensaje de eliminación exitosa
            return ResponseEntity.ok("Categoria eliminada con ID: " + productId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrada con ID: " + productId);
        }
    }

}

