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
    @Autowired
    private ConvertDTO convertDTO;

    @GetMapping()
    public ResponseEntity<Page<ProductDTO>> findByCategoryName(@RequestParam(required = false) String categoryName, Pageable pageable) {
        Page<Product> productsPage;

        if (categoryName != null && !categoryName.isEmpty()) {
            productsPage = productService.findByCategoryName(categoryName, pageable);
        } else {
            productsPage = productService.findAll(pageable);
        }

        Page<ProductDTO> productDTOs = productsPage.map(convertDTO::convertToProductDTO);

        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return productService.findById(id)
                .map(convertDTO::convertToProductDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody ProductDTO productDTO) {
        Product existingProduct = productService.findByNameAndColor(productDTO.getName(), productDTO.getColor());

        if (existingProduct != null) {
            existingProduct.setStock(existingProduct.getStock() + productDTO.getStock());
            Product updatedProduct = productService.save(existingProduct);
            ProductDTO updatedProductDTO = convertDTO.convertToProductDTO(updatedProduct);
            return ResponseEntity.ok(updatedProductDTO);
        } else {
            Product newProduct = new Product();
            newProduct.setName(productDTO.getName());
            newProduct.setColor(productDTO.getColor());
            newProduct.setStock(productDTO.getStock());

            Category category = categoryService.findByName(productDTO.getCategory());

            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La categoría no existe");
            }

            newProduct.setCategory(category);
            productService.save(newProduct);
            ProductDTO newProductDTO = convertDTO.convertToProductDTO(newProduct);
            return ResponseEntity.status(HttpStatus.CREATED).body(newProductDTO);
        }
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<?> update(@PathVariable Long productId, @RequestBody ProductDTO productDTO) {
        Optional<Product> optionalProduct = productService.findById(productId);

        if (optionalProduct.isPresent()) {
            Product existingProduct = optionalProduct.get();

            existingProduct.setName(productDTO.getName());
            existingProduct.setColor(productDTO.getColor());
            existingProduct.setStock(productDTO.getStock());

            Category category = categoryService.findByName(productDTO.getCategory());

            if (category != null) {
                existingProduct.setCategory(category);
                Product updatedProduct = productService.save(existingProduct);
                ProductDTO updatedProductDTO = convertDTO.convertToProductDTO(updatedProduct);
                return ResponseEntity.ok(updatedProductDTO);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoría no encontrada: " + productDTO.getCategory());
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado con ID: " + productId);
        }
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<?> delete(@PathVariable Long productId) {
        Optional<Product> optionalProduct = productService.findById(productId);

        if (optionalProduct.isPresent()) {
            productService.deleteById(productId);
            return ResponseEntity.ok("Categoria eliminada con ID: " + productId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrada con ID: " + productId);
        }
    }
}


