package com.chiton.api.controller;

import com.chiton.api.dto.ProductDTO;
import com.chiton.api.entity.*;
import com.chiton.api.service.CategoryService;
import com.chiton.api.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> findAll(){
        List<Product> products = productService.findAll();
        List<ProductDTO> productDTOS = products.stream()
                .map(convertDTO::convertToProductDTO)
                .toList();
        return ResponseEntity.ok(productDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return productService.findById(id)
                .map(product ->ResponseEntity.ok(convertDTO.convertToProductDTO(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody ProductDTO productDTO) {
        Product existingProduct = productService.findByNameAndColor(productDTO.getName(), productDTO.getColor());

        if (existingProduct != null) {
            existingProduct.setStock(existingProduct.getStock() + productDTO.getStock());
            Category category = categoryService.findByName(productDTO.getCategory());

            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La categoría no existe");
            }
            existingProduct.setCategory(category);
            Product updatedProduct = productService.save(existingProduct);
            ProductDTO updatedProductDTO = convertDTO.convertToProductDTO(updatedProduct);
            return ResponseEntity.ok(updatedProductDTO);
        } else {
            Product newProduct = new Product();
            newProduct.setName(productDTO.getName());
            newProduct.setColor(productDTO.getColor());
            newProduct.setStock(productDTO.getStock());
            newProduct.setStatus("Activado");

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

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        Optional<Product> optionalProduct = productService.findById(id);

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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }

    @PostMapping("/status/{id}")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        Optional<Product> optionalProduct = productService.findById(id);

        if (optionalProduct.isPresent()) {
            Product existingProduct = optionalProduct.get();

            // Cambiar el estado de la categoría
            String currentStatus = existingProduct.getStatus();
            String newStatus = currentStatus.equals("Activado") ? "Desactivado" : "Activado";
            existingProduct.setStatus(newStatus);

            productService.save(existingProduct);
            String message = newStatus.equals("Activado") ? "Producto activado" : "Producto desactivado";
            return ResponseEntity.ok().body(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }

    @PutMapping("/add/{id}/{stock}")
    public ResponseEntity<?> addStock(@PathVariable Long id, @PathVariable Double stock) {
        if (stock <= 0) {
            return ResponseEntity.badRequest().body("La cantidad de stock debe ser un número positivo");
        }

        Optional<Product> optionalProduct = productService.findById(id);
        if (optionalProduct.isPresent()) {
            Product existingProduct = optionalProduct.get();
            Double currentStock = existingProduct.getStock();
            existingProduct.setStock(currentStock + stock);
            productService.save(existingProduct);
            return ResponseEntity.ok().body("Cantidad agregada");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró ningún producto con el ID proporcionado: " + id);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        Optional<Product> optionalProduct = productService.findById(id);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();

            // Obtener los IDs de las referencias relacionadas
            List<Long> referenceDetailIds = product.getReferenceDetails().stream()
                    .map(referenceDetail -> referenceDetail.getReference().getId())
                    .toList();

            // Obtener los IDs de las órdenes de compra relacionadas
            List<Long> purchaseDetailIds = product.getPurchaseDetails().stream()
                    .map(purchaseDetail -> purchaseDetail.getPurchaseOrder().getId())
                    .toList();

            // Verificar si hay detalles de referencia o compra relacionados con este producto
            if (!referenceDetailIds.isEmpty() || !purchaseDetailIds.isEmpty()) {
                Map<String, List<Long>> relatedDetails = new HashMap<>();
                relatedDetails.put("References", referenceDetailIds);
                relatedDetails.put("Purchases", purchaseDetailIds);
                return ResponseEntity.badRequest().body(relatedDetails);
            }

            // Si no hay detalles de referencia ni compra asociados, proceder con la eliminación
            product.setCategory(null);
            productService.save(product);
            productService.deleteById(id);
            return ResponseEntity.ok("Producto eliminado");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }
}


