package com.chiton.api.controller;

import com.chiton.api.dto.ReferenceDTO;
import com.chiton.api.dto.ReferenceDetailDTO;
import com.chiton.api.entity.*;
import com.chiton.api.service.CustomerService;
import com.chiton.api.service.ProductService;
import com.chiton.api.service.ReferenceService;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/references")
public class ReferenceController {

    @Autowired
    private ReferenceService referenceService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ConvertDTO convertDTO;

    @Autowired
    private ProductService productService;

    @GetMapping()
    public ResponseEntity<?> findAll(){
        List<Reference> references = referenceService.findAll();
        List<ReferenceDTO> referenceDTOS = references.stream()
                .map(convertDTO::convertToReferenceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(referenceDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        return referenceService.findById(id).map(ref -> ResponseEntity.ok(convertDTO.convertToReferenceDTO(ref)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody ReferenceDTO referenceDTO) {

        // Verificar si el cliente existe
        Customer customer = customerService.findByName(referenceDTO.getCustomer());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El cliente no existe");
        }

        // Crear la nueva referencia
        Reference newReference = new Reference();
        newReference.setCustomer(customer);
        newReference.setDescription(referenceDTO.getDescription());
        newReference.setImage(referenceDTO.getImage());

        // Validar la existencia de los productos en los detalles y manejar duplicados
        Map<String, ReferenceDetail> productDetailsMap = new HashMap<>();
        ResponseEntity<?> detailDTO = handleReferenceDetails(referenceDTO, newReference, productDetailsMap);
        if (detailDTO != null) return detailDTO;

        // Asociar los detalles a la referencia y guardar todo dentro de una transacción
        List<ReferenceDetail> referenceDetails = new ArrayList<>(productDetailsMap.values());
        newReference.setDetail(referenceDetails);
        Reference savedReference = referenceService.save(newReference);

        return ResponseEntity
                .created(URI.create("/api/references/" + savedReference.getId()))
                .body(convertDTO.convertToReferenceDTO(savedReference));
    }

    @PutMapping("/update/{referenceId}")
    public ResponseEntity<?> update(@PathVariable Long referenceId, @RequestBody ReferenceDTO updatedReferenceDTO) {

        // Buscar la referencia existente
        Optional<Reference> optionalExistingReference = referenceService.findById(referenceId);

        // Verificar si la referencia existe
        if (optionalExistingReference.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La referencia no existe");
        }

        // Obtener la referencia del Optional
        Reference existingReference = optionalExistingReference.get();

        // Verificar si el cliente existe
        Customer customer = customerService.findByName(updatedReferenceDTO.getCustomer());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El cliente no existe");
        }

        // Actualizar la referencia con los nuevos datos
        existingReference.setCustomer(customer);
        existingReference.setDescription(updatedReferenceDTO.getDescription());
        existingReference.setImage(updatedReferenceDTO.getImage());

        // Obtener los detalles existentes en la referencia
        List<ReferenceDetail> existingDetails = existingReference.getDetail();

        // Agregar los detalles existentes al mapa para su verificación
        Map<String, ReferenceDetail> productDetailsMap = new HashMap<>();
        for (ReferenceDetail existingDetail : existingDetails) {
            Product existingProduct = existingDetail.getProduct();
            String existingProductName = existingProduct.getName(); // Ajusta según la estructura de tu clase Product

            // Agregar el detalle existente al mapa
            productDetailsMap.put(existingProductName, existingDetail);
        }

        // Verificar y procesar los nuevos detalles del DTO
        ResponseEntity<?> detailDTO = handleReferenceDetails(updatedReferenceDTO, existingReference, productDetailsMap);
        if (detailDTO != null) return detailDTO;


        // Actualizar y guardar la referencia
        existingReference = referenceService.save(existingReference);

        // Crear y asociar los detalles de referencia
        if (productDetailsMap.isEmpty()) {
            // Si la lista de detalles está vacía, eliminar todos los detalles asociados a la referencia
            List<ReferenceDetail> currentDetails = existingReference.getDetail();
            List<ReferenceDetail> newDetails = new ArrayList<>(productDetailsMap.values());

            // Eliminar detalles que ya no están presentes
            currentDetails.removeIf(detail -> !newDetails.contains(detail));

            existingReference.setDetail(currentDetails);
        } else {
            // Si la lista de detalles no está vacía, crear y asociar los nuevos detalles
            List<ReferenceDetail> referenceDetails = new ArrayList<>(productDetailsMap.values());

            // Asociar los detalles a la referencia y guardarlos
            existingReference.setDetail(referenceDetails);
        }

        existingReference = referenceService.save(existingReference);
        return ResponseEntity.ok(convertDTO.convertToReferenceDTO(existingReference));
    }

    private ResponseEntity<?> handleReferenceDetails(ReferenceDTO updatedReferenceDTO, Reference existingReference, Map<String, ReferenceDetail> productDetailsMap) {
        for (ReferenceDetailDTO detailDTO : updatedReferenceDTO.getDetails()) {
            Product product = productService.findByName(detailDTO.getProduct());
            if (product == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El producto " + detailDTO.getProduct() + " en los detalles no existe");
            }

            // Verificar si ya existe un detalle para el mismo producto
            String productName = detailDTO.getProduct();
            ReferenceDetail existingDetail = productDetailsMap.get(productName);
            if (existingDetail != null) {
                // Sumar la cantidad al detalle existente
                existingDetail.setQuantity(existingDetail.getQuantity() + detailDTO.getQuantity());
            } else {
                // Crear un nuevo detalle y agregarlo al mapa
                ReferenceDetail newDetail = convertDTO.convertToReferenceDetail(detailDTO, existingReference);
                newDetail.setProduct(product);
                productDetailsMap.put(productName, newDetail);
            }
        }
        return null;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteReference(@PathVariable Long id) {
        try {
            referenceService.deleteById(id);
            return ResponseEntity.ok("Referencia eliminada exitosamente.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la referencia.");
        }
    }
}