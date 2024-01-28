package com.chiton.api.controller;

import com.chiton.api.dto.ProductionDetailDTO;
import com.chiton.api.dto.ProductionOrderDTO;
import com.chiton.api.entity.*;
import com.chiton.api.service.CustomerService;
import com.chiton.api.service.ProductionOrderService;
import com.chiton.api.service.ReferenceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/productionOrders")
public class ProductionOrderController {

    @Autowired
    private ProductionOrderService productionOrderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ReferenceService referenceService;

    @Autowired
    private ConvertDTO convertDTO;

    @GetMapping()
    public ResponseEntity<?> findAll(){
        List<ProductionOrder> productionOrders = productionOrderService.findAll();
        List<ProductionOrderDTO> productionOrderDTOS = productionOrders.stream()
                .map(convertDTO::convertToProductionOrderDTO)
                .toList();
        return ResponseEntity.ok(productionOrderDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        return productionOrderService.findById(id)
                .map(prod -> ResponseEntity.ok(convertDTO.convertToProductionOrderDTO(prod)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody ProductionOrderDTO productionOrderDTO) {

        // Verificar si el cliente existe
        Customer customer = customerService.findByName(productionOrderDTO.getCustomer());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El cliente no existe");
        }

        //Crear la nueva orden de produccion
        ProductionOrder newProductionOrder = new ProductionOrder();
        newProductionOrder.setCustomer(customer);

        LocalDate gendate = LocalDate.now(ZoneId.of("America/Lima"));
        newProductionOrder.setGenerationDate(gendate);
        newProductionOrder.setDeadline(productionOrderDTO.getDeadline());

        // Mapa para realizar un seguimiento de los detalles del JSON por referencia
        Map<Long, ProductionDetailDTO> referenceDetailsMap = new HashMap<>();

        // Combinar cantidades de detalles con la mismo referencia
        for (ProductionDetailDTO detailDTO : productionOrderDTO.getDetails()) {
            Long refId = detailDTO.getReference();
            if (referenceDetailsMap.containsKey(refId)) {
                // Si ya existe, sumar la cantidad al detalle existente
                ProductionDetailDTO existingDetailDTO = referenceDetailsMap.get(refId);
                existingDetailDTO.setQuantity(existingDetailDTO.getQuantity() + detailDTO.getQuantity());
            } else {
                // Si no existe, agregar el detalle al mapa
                referenceDetailsMap.put(refId, detailDTO);
            }
        }
        // Asociar los detalles a la orden de produccion
        List<ProductionDetail> productionDetails = new ArrayList<>();

        for (ProductionDetailDTO detailDTO : referenceDetailsMap.values()) {
            Optional<Reference> reference = referenceService.findById(detailDTO.getReference());
            if (reference.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La referencia con ID:" + detailDTO.getReference() + " no existe");
            }

            ProductionDetail newDetail = convertDTO.convertToProductionDetail(detailDTO, newProductionOrder);
            newDetail.setReference(reference.get());
            productionDetails.add(newDetail);
        }

        newProductionOrder.setDetails(productionDetails);

        // Guardar todo dentro de una transacción
        ProductionOrder savedProdOrder = productionOrderService.save(newProductionOrder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertDTO.convertToProductionOrderDTO(savedProdOrder));
    }

    @Transactional
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductionOrderDTO updatedProdDTO){

        //Buscar la orden de Produccion existente
        Optional<ProductionOrder> optionalProductionOrder = productionOrderService.findById(id);

        //Verificar si exise la orden de Produccion
        if(optionalProductionOrder.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La orden de Produccion no existe");
        }

        //Obtener la orden de compra del Optional
        ProductionOrder existingprodOrder = optionalProductionOrder.get();

        // Verificar si el cliente existe
        Customer customer = customerService.findByName(updatedProdDTO.getCustomer());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El cliente no existe");
        }

        //Actualizar la orden de produccion con los nuevos datos
        existingprodOrder.setCustomer(customer);
        existingprodOrder.setDeadline(updatedProdDTO.getDeadline());

        // Mapa para realizar un seguimiento de los detalles del JSON por referencia
        Map<Long, ProductionDetailDTO> referenceDetailsMap = new HashMap<>();

        // Combinar cantidades de detalles con la mismo referencia
        for (ProductionDetailDTO detailDTO : updatedProdDTO.getDetails()) {
            Long refId = detailDTO.getReference();
            if (referenceDetailsMap.containsKey(refId)) {
                // Si ya existe, sumar la cantidad al detalle existente
                ProductionDetailDTO existingDetailDTO = referenceDetailsMap.get(refId);
                existingDetailDTO.setQuantity(existingDetailDTO.getQuantity() + detailDTO.getQuantity());
            } else {
                // Si no existe, agregar el detalle al mapa
                referenceDetailsMap.put(refId, detailDTO);
            }
        }

        // Verificar y procesar los detalles combinados del mapa
        for (ProductionDetailDTO detailDTO : referenceDetailsMap.values()) {
            Optional<Reference> reference = referenceService.findById(detailDTO.getReference());
            if (reference.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La referencia " + detailDTO.getReference() + " no existe");
            }

            // Verificar si ya existe un detalle para el mismo producto
            Long refId = detailDTO.getReference();
            ProductionDetail existingDetail = getExistingDetail(existingprodOrder, refId);

            if (existingDetail != null) {
                // Si existe, actualizar la cantidad del detalle existente con la nueva cantidad
                existingDetail.setQuantity(detailDTO.getQuantity());
            } else {
                // Si no existe, crear un nuevo detalle y agregarlo a la lista
                ProductionDetail newDetail = convertDTO.convertToProductionDetail(detailDTO, existingprodOrder);
                newDetail.setReference(reference.get());
                existingprodOrder.getDetails().add(newDetail);
            }
        }
        // Eliminar detalles que ya no están presentes
        existingprodOrder.getDetails().removeIf(detail ->
                referenceDetailsMap.values().stream()
                        .noneMatch(dto -> dto.getReference().equals(detail.getReference().getId())));

        // Guardar la referencia actualizada
        existingprodOrder = productionOrderService.save(existingprodOrder);


        return ResponseEntity.ok(convertDTO.convertToProductionOrderDTO(existingprodOrder));

    }

    private ProductionDetail getExistingDetail(ProductionOrder existingProdOrder, Long refId) {
        for (ProductionDetail detail : existingProdOrder.getDetails()) {
            if (detail.getReference().getId().equals(refId)) {
                return detail;
            }
        }
        return null;
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        Optional<ProductionOrder> optionalProductionOrder = productionOrderService.findById(id);

        if(optionalProductionOrder.isPresent()){
            ProductionOrder productionOrder = optionalProductionOrder.get();

            productionOrder.getDetails().clear();
            productionOrder.setTranslateOrder(null);
            productionOrderService.deleteById(id);
            return ResponseEntity.ok("Orden de produccion eliminada");
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Orden de produccion no encontrado");
        }
    }
}
