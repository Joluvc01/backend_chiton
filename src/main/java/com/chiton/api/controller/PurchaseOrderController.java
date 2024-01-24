package com.chiton.api.controller;


import com.chiton.api.dto.PurchaseDetailDTO;
import com.chiton.api.dto.PurchaseOrderDTO;
import com.chiton.api.dto.ReferenceDetailDTO;
import com.chiton.api.entity.*;
import com.chiton.api.service.ProductService;
import com.chiton.api.service.PurchaseOrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.Date;
import java.time.LocalDate;
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
        return purchaseOrderService.findById(id).map(ord -> ResponseEntity.ok(convertDTO.convertToPurchaseOrderDTO(ord)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PurchaseOrderDTO purchaseOrderDTO){

        //Crear nueva orden de Compra
        PurchaseOrder newpurchaseOrder = new PurchaseOrder();
        Date date = Date.valueOf(LocalDate.now());
        newpurchaseOrder.setGenerationDate(date);

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
                .created(URI.create("/api/references/" + savedpurchaseOrder.getId()))
                .body(convertDTO.convertToPurchaseOrderDTO(savedpurchaseOrder));

    }

    @Transactional
    @PutMapping("/update/{purchaseId}")
    public ResponseEntity<?> update(@PathVariable Long purchaseId, @RequestBody PurchaseOrderDTO updatedpurchaseOrderDTO){

        // Buscar la orden de compra existente
        Optional<PurchaseOrder> optionalExistingPurchase = purchaseOrderService.findById(purchaseId);

        // Verificar si la orden de compra existe
        if (optionalExistingPurchase.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La orden de compra no existe");
        }

        // Obtener la referencia del Optional
        PurchaseOrder existingPurchase = optionalExistingPurchase.get();

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
}
