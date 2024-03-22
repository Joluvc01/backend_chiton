package com.chiton.api.controller;

import com.chiton.api.dto.ProductionDetailDTO;
import com.chiton.api.dto.ProductionOrderDTO;
import com.chiton.api.entity.*;
import com.chiton.api.service.*;
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
        checkAndUpdateDelayedOrders();
        List<ProductionOrder> productionOrders = productionOrderService.findAll();
        List<ProductionOrderDTO> productionOrderDTOS = productionOrders.stream()
                .map(convertDTO::convertToProductionOrderDTO)
                .toList();
        return ResponseEntity.ok(productionOrderDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        checkAndUpdateDelayedOrders();
        return productionOrderService.findById(id)
                .map(prod -> ResponseEntity.ok(convertDTO.convertToProductionOrderDTO(prod)))
                .orElse(ResponseEntity.notFound().build());
    }

    private void checkAndUpdateDelayedOrders(){
        List<ProductionOrder> productionOrders = productionOrderService.findAll();
        LocalDate currentDate = LocalDate.now();

        for (ProductionOrder order : productionOrders) {
            if (order.getStatus().equals("Incompleto") && currentDate.isAfter(order.getDeadline())) {
                order.setStatus("Retrasado");
                productionOrderService.save(order);
            }
        }
    }


    @Transactional
    @PostMapping()
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
        newProductionOrder.setStatus("Incompleto");

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
    @PutMapping("/{id}")
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

    @PostMapping("/status/{id}")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        Optional<ProductionOrder> optionalProductionOrder = productionOrderService.findById(id);

        if (optionalProductionOrder.isPresent()) {
            ProductionOrder productionOrder = optionalProductionOrder.get();
            if (productionOrder.getStatus().equals("Completo")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Esta Orden de Produccion ya fue completada");
            } else {
                Map<Product, Double> productRequirements = calculateProductRequirements(productionOrder);
                if (productRequirements.isEmpty()) {
                    productionOrder.setStatus("Completo");
                    LocalDate compdate = LocalDate.now(ZoneId.of("America/Lima"));
                    productionOrder.setCompletedDate(compdate);
                    System.out.println(compdate);
                    productionOrderService.save(productionOrder);
                    return ResponseEntity.ok().body("Orden de Produccion completa.");
                } else {
                    StringBuilder message = new StringBuilder("Stock insuficiente para los siguientes productos:");
                    for (Map.Entry<Product, Double> entry : productRequirements.entrySet()) {
                        Product product = entry.getKey();
                        Double quantityNeeded = entry.getValue();
                        double stockShortage = quantityNeeded - product.getStock();
                        message.append(product.getName()).append(" = ").append(stockShortage).append(" unidades");
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message.toString());
                }
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Orden de Produccion no encontrada");
        }
    }

    private Map<Product, Double> calculateProductRequirements(ProductionOrder productionOrder) {
        Map<Product, Double> productRequirements = new HashMap<>();
        for (ProductionDetail productionDetail : productionOrder.getDetails()) {
            Reference reference = productionDetail.getReference();
            for (ReferenceDetail referenceDetail : reference.getDetails()) {
                Product product = referenceDetail.getProduct();
                double quantityNeeded = referenceDetail.getQuantity() * productionDetail.getQuantity();
                if (product.getStock() < quantityNeeded) {
                    productRequirements.put(product, quantityNeeded);
                } else {
                    // Restar del stock actual si el stock es mayor o igual a la cantidad requerida de los productos
                    double newStock = product.getStock() - quantityNeeded;
                    product.setStock(newStock);
                }
            }
        }
        return productRequirements;
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        Optional<ProductionOrder> optionalProductionOrder = productionOrderService.findById(id);

        if (optionalProductionOrder.isPresent()) {
            ProductionOrder productionOrder = optionalProductionOrder.get();
            productionOrder.getDetails().clear();
            TranslateOrder translateOrder = productionOrder.getTranslateOrder();
            if (translateOrder == null) {
                productionOrderService.deleteById(id);
                return ResponseEntity.ok("Orden de producción eliminada");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(translateOrder.getId());
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Orden de producción no encontrada");
        }
    }
}
