package com.chiton.api.controller;

import com.chiton.api.dto.ReferenceDTO;
import com.chiton.api.entity.*;
import com.chiton.api.service.CustomerService;
import com.chiton.api.service.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

        // Guardar la referencia
        Reference savedReference = referenceService.save(newReference);

        // Crear y asociar los detalles de referencia
        if (referenceDTO.getDetails() != null && !referenceDTO.getDetails().isEmpty()) {
            List<ReferenceDetail> referenceDetails = referenceDTO.getDetails().stream()
                    .map(detailDTO -> convertDTO.convertToReferenceDetail(detailDTO, savedReference))
                    .collect(Collectors.toList());

            // Asociar los detalles a la referencia y guardarlos
            savedReference.setDetail(referenceDetails);
            referenceService.save(savedReference);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedReference);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ReferenceDTO referenceDTO) {
        Optional<Reference> optionalReference = referenceService.findById(id);

        if (optionalReference.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La referencia con ID " + id + " no existe");
        }

        Reference existingReference = optionalReference.get();

        Customer customer = customerService.findByName(referenceDTO.getCustomer());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El cliente no existe");
        }
        existingReference.setCustomer(customer);
        existingReference.setDescription(referenceDTO.getDescription());
        existingReference.setImage(referenceDTO.getImage());

        Reference updatedReference = referenceService.save(existingReference);

        return ResponseEntity.status(HttpStatus.OK).body(updatedReference);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<Reference> optionalReference = referenceService.findById(id);

        if (optionalReference.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La referencia con ID " + id + " no existe");
        }

        referenceService.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Referencia eliminada con éxito");
    }
}