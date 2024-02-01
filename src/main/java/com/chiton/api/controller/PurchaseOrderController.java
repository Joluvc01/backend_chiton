package com.chiton.api.controller;


import com.chiton.api.dto.PurchaseDetailDTO;
import com.chiton.api.dto.PurchaseOrderDTO;
import com.chiton.api.entity.*;
import com.chiton.api.service.ProductService;
import com.chiton.api.service.PurchaseOrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/purchaseOrders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ConvertDTO convertDTO;

    @GetMapping()
    public ResponseEntity<?> findAll(){
        List<PurchaseOrder> orders = purchaseOrderService.findAll();
        List<PurchaseOrderDTO> orderDTOS = orders.stream()
                .map(convertDTO::convertToPurchaseOrderDTO)
                .toList();
        return ResponseEntity.ok(orderDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        return purchaseOrderService.findById(id)
                .map(ord -> ResponseEntity.ok(convertDTO.convertToPurchaseOrderDTO(ord)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<?> create(@RequestBody PurchaseOrderDTO purchaseOrderDTO){

        //Crear nueva orden de Compra
        PurchaseOrder newpurchaseOrder = new PurchaseOrder();
        LocalDate date = LocalDate.now(ZoneId.of("America/Lima"));
        newpurchaseOrder.setGenerationDate(date);
        newpurchaseOrder.setCompleted(false);

        // Mapa para realizar un seguimiento de los detalles del JSON por producto
        Map<String, PurchaseDetailDTO> productsDetailMap = new HashMap<>();

        // Combinar cantidades de detalles con el mismo producto
        for (PurchaseDetailDTO detailDTO : purchaseOrderDTO.getDetails()){
            String productName = detailDTO.getProduct();
            if(productsDetailMap.containsKey(productName)){
                //Si ya exist, sumar la cantidad al detalle existente
                PurchaseDetailDTO existingDetailDTO = productsDetailMap.get(productName);
                existingDetailDTO.setQuantity(existingDetailDTO.getQuantity() + detailDTO.getQuantity());
            } else {
                //Si el producto no existe, agregar el detalle al mapa
                productsDetailMap.put(productName, detailDTO);
            }
        }

        //Asociar los detalles al orden de compra
        List<PurchaseDetail> purchaseDetails = new ArrayList<>();

        for (PurchaseDetailDTO detailDTO : productsDetailMap.values()){
            Product product = productService.findByName(detailDTO.getProduct());
            if (product == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El producto" + detailDTO.getProduct() + " no existe");
            }

            PurchaseDetail newDetail = convertDTO.converToPurchaseDetail(detailDTO, newpurchaseOrder);
            newDetail.setProduct(product);
            purchaseDetails.add(newDetail);
        }

        newpurchaseOrder.setDetails(purchaseDetails);

        //Guardar todo dentro de una transaccion
        PurchaseOrder savedpurchaseOrder = purchaseOrderService.save(newpurchaseOrder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertDTO.convertToPurchaseOrderDTO(savedpurchaseOrder));

    }

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long purchaseId, @RequestBody PurchaseOrderDTO updatedpurchaseOrderDTO){

        // Buscar la orden de compra existente
        Optional<PurchaseOrder> optionalExistingPurchase = purchaseOrderService.findById(purchaseId);

        // Verificar si la orden de compra existe
        if (optionalExistingPurchase.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La orden de compra no existe");
        }

        // Obtener la referencia del Optional
        PurchaseOrder existingPurchase = optionalExistingPurchase.get();
        existingPurchase.setCompleted(false);

        // Mapa para realizar un seguimiento de los detalles del JSON por producto
        Map<String, PurchaseDetailDTO> productDetailsMap = new HashMap<>();

        // Combinar cantidades de detalles con el mismo producto
        for (PurchaseDetailDTO detailDTO : updatedpurchaseOrderDTO.getDetails()) {
            String productName = detailDTO.getProduct();
            if (productDetailsMap.containsKey(productName)) {
                // Si ya existe, sumar la cantidad al detalle existente
                PurchaseDetailDTO existingDetailDTO = productDetailsMap.get(productName);
                existingDetailDTO.setQuantity(existingDetailDTO.getQuantity() + detailDTO.getQuantity());
            } else {
                // Si no existe, agregar el detalle al mapa
                productDetailsMap.put(productName, detailDTO);
            }
        }

        // Verificar y procesar los detalles combinados del mapa
        for (PurchaseDetailDTO detailDTO : productDetailsMap.values()) {
            Product product = productService.findByName(detailDTO.getProduct());
            if (product == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El producto " + detailDTO.getProduct() + " no existe");
            }

            // Verificar si ya existe un detalle para el mismo producto
            String productName = detailDTO.getProduct();
            PurchaseDetail existingDetail = getExistingDetail(existingPurchase, productName);

            if (existingDetail != null) {
                // Si existe, actualizar la cantidad del detalle existente con la nueva cantidad
                existingDetail.setQuantity(detailDTO.getQuantity());
            } else {
                // Si no existe, crear un nuevo detalle y agregarlo a la lista
                PurchaseDetail newDetail = convertDTO.converToPurchaseDetail(detailDTO, existingPurchase);
                newDetail.setProduct(product);
                existingPurchase.getDetails().add(newDetail);
            }
        }

        // Eliminar detalles que ya no están presentes
        existingPurchase.getDetails().removeIf(detail ->
                productDetailsMap.values().stream()
                        .noneMatch(dto -> dto.getProduct().equals(detail.getProduct().getName())));

        // Guardar la referencia actualizada
        existingPurchase = purchaseOrderService.save(existingPurchase);


        return ResponseEntity.ok(convertDTO.convertToPurchaseOrderDTO(existingPurchase));
    }

    // Método para obtener el detalle existente por nombre de producto
    private PurchaseDetail getExistingDetail(PurchaseOrder existingPurchaseOrder, String productName){
        for (PurchaseDetail detail : existingPurchaseOrder.getDetails()){
            if (detail.getProduct().getName().equals(productName)){
                return detail;
            }
        }
        return null;
    }

    @PatchMapping("/completed/{id}")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        Optional<PurchaseOrder> optionalPurchaseOrder = purchaseOrderService.findById(id);

        if (optionalPurchaseOrder.isPresent()) {
            PurchaseOrder existingPurchaseOrder = optionalPurchaseOrder.get();
            if (existingPurchaseOrder.getCompleted()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Esta orden de compra ya fue completada");
            } else {
                // Cambiar el estado de la orden de compra
                existingPurchaseOrder.setCompleted(true);

                // Recorrer la lista de detalles de la orden de compra
                for (PurchaseDetail detail : existingPurchaseOrder.getDetails()) {
                    // Obtener el producto y la cantidad comprada
                    Product product = detail.getProduct();
                    double purchasedQuantity = detail.getQuantity();

                    // Actualizar el stock del producto
                    double currentStock = product.getStock();
                    product.setStock(currentStock + purchasedQuantity);

                    // Guardar el producto actualizado en la base de datos
                    productService.save(product);
                }

                // Guardar la orden de compra actualizada en la base de datos
                purchaseOrderService.save(existingPurchaseOrder);

                return ResponseEntity.ok().body("Orden de compra completa. Stock actualizado.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Orden de compra no encontrada no encontrado");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        Optional<PurchaseOrder> optionalPurchaseOrder = purchaseOrderService.findById(id);

        if(optionalPurchaseOrder.isPresent()){
            PurchaseOrder purchaseOrder = optionalPurchaseOrder.get();
            purchaseOrder.getDetails().clear();
            purchaseOrderService.deleteById(id);
            return ResponseEntity.ok("Orden de compra eliminada");
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Orden de compra no encontrada");
        }
    }
}
