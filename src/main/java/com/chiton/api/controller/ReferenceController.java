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
    private ProductService productService;

    @Autowired
    private ConvertDTO convertDTO;

    @GetMapping()
    public ResponseEntity<?> findAll(){
        List<Reference> references = referenceService.findAll();
        List<ReferenceDTO> referenceDTOS = references.stream()
                .map(convertDTO::convertToReferenceDTO)
                .toList();
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

        // Crear la nueva referencia
        Reference newReference = new Reference();
        newReference.setDescription(referenceDTO.getDescription());
        newReference.setImage(referenceDTO.getImage());

        // Mapa para realizar un seguimiento de los detalles del JSON por producto
        Map<String, ReferenceDetailDTO> productDetailsMap = new HashMap<>();

        // Combinar cantidades de detalles con el mismo producto
        for (ReferenceDetailDTO detailDTO : referenceDTO.getDetails()) {
            String productName = detailDTO.getProduct();
            if (productDetailsMap.containsKey(productName)) {
                // Si ya existe, sumar la cantidad al detalle existente
                ReferenceDetailDTO existingDetailDTO = productDetailsMap.get(productName);
                existingDetailDTO.setQuantity(existingDetailDTO.getQuantity() + detailDTO.getQuantity());
            } else {
                // Si no existe, agregar el detalle al mapa
                productDetailsMap.put(productName, detailDTO);
            }
        }

        // Asociar los detalles a la referencia
        List<ReferenceDetail> referenceDetails = new ArrayList<>();

        for (ReferenceDetailDTO detailDTO : productDetailsMap.values()) {
            Product product = productService.findByName(detailDTO.getProduct());
            if (product == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El producto " + detailDTO.getProduct() + " no existe");
            }

            ReferenceDetail newDetail = convertDTO.convertToReferenceDetail(detailDTO, newReference);
            newDetail.setProduct(product);
            referenceDetails.add(newDetail);
        }

        newReference.setDetails(referenceDetails);

        // Guardar todo dentro de una transacción
        Reference savedReference = referenceService.save(newReference);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertDTO.convertToReferenceDTO(savedReference));
    }


    @Transactional
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

        // Actualizar la referencia con los nuevos datos
        existingReference.setDescription(updatedReferenceDTO.getDescription());
        existingReference.setImage(updatedReferenceDTO.getImage());

        // Mapa para realizar un seguimiento de los detalles del JSON por producto
        Map<String, ReferenceDetailDTO> productDetailsMap = new HashMap<>();

        // Combinar cantidades de detalles con el mismo producto
        for (ReferenceDetailDTO detailDTO : updatedReferenceDTO.getDetails()) {
            String productName = detailDTO.getProduct();
            if (productDetailsMap.containsKey(productName)) {
                // Si ya existe, sumar la cantidad al detalle existente
                ReferenceDetailDTO existingDetailDTO = productDetailsMap.get(productName);
                existingDetailDTO.setQuantity(existingDetailDTO.getQuantity() + detailDTO.getQuantity());
            } else {
                // Si no existe, agregar el detalle al mapa
                productDetailsMap.put(productName, detailDTO);
            }
        }

        // Verificar y procesar los detalles combinados del mapa
        for (ReferenceDetailDTO detailDTO : productDetailsMap.values()) {
            Product product = productService.findByName(detailDTO.getProduct());
            if (product == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El producto " + detailDTO.getProduct() + " no existe");
            }

            // Verificar si ya existe un detalle para el mismo producto
            String productName = detailDTO.getProduct();
            ReferenceDetail existingDetail = getExistingDetail(existingReference, productName);

            if (existingDetail != null) {
                // Si existe, actualizar la cantidad del detalle existente con la nueva cantidad
                existingDetail.setQuantity(detailDTO.getQuantity());
            } else {
                // Si no existe, crear un nuevo detalle y agregarlo a la lista
                ReferenceDetail newDetail = convertDTO.convertToReferenceDetail(detailDTO, existingReference);
                newDetail.setProduct(product);
                existingReference.getDetails().add(newDetail);
            }
        }

        // Eliminar detalles que ya no están presentes
        existingReference.getDetails().removeIf(detail ->
                productDetailsMap.values().stream()
                        .noneMatch(dto -> dto.getProduct().equals(detail.getProduct().getName())));

        // Guardar la referencia actualizada
        existingReference = referenceService.save(existingReference);


        return ResponseEntity.ok(convertDTO.convertToReferenceDTO(existingReference));
    }

    // Método para obtener el detalle existente por nombre de producto
    private ReferenceDetail getExistingDetail(Reference existingReference, String productName) {
        for (ReferenceDetail detail : existingReference.getDetails()) {
            if (detail.getProduct().getName().equals(productName)) {
                return detail;
            }
        }
        return null;
    }
}