package com.chiton.api.controller;


import com.chiton.api.dto.PurchaseDetailDTO;
import com.chiton.api.dto.PurchaseOrderDTO;
import com.chiton.api.entity.Product;
import com.chiton.api.entity.PurchaseDetail;
import com.chiton.api.entity.PurchaseOrder;
import com.chiton.api.service.ProductService;
import com.chiton.api.service.PurchaseOrderService;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PurchaseOrderDTO purchaseOrderDTO) throws ParseException {

        // Crear la nueva orden de Compra
        PurchaseOrder newOrder = new PurchaseOrder();
        Date currentDate = new Date();

        // Formatear la fecha al formato "dd-MM-yyyy"
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = sdf.format(currentDate);
        Date parsedDate = sdf.parse(formattedDate);
        newOrder.setGeneration_date(parsedDate);

        // Validar la existencia de los productos en los detalles y manejar duplicados
        Map<String, PurchaseDetail> productDetailsMap = new HashMap<>();
        ResponseEntity<?> detailDTO = hanldePurchaseDetails(purchaseOrderDTO, newOrder, productDetailsMap);
        if (detailDTO != null) return detailDTO;

        // Asociar los detalles a la referencia y guardar todo dentro de una transacción
        List<PurchaseDetail> purchaseDetails = new ArrayList<>(productDetailsMap.values());
        newOrder.setDetails(purchaseDetails);
        PurchaseOrder savedPurchaseOrder = purchaseOrderService.save(newOrder);

        return ResponseEntity
                .created(URI.create("/api/purchaseOrders" + savedPurchaseOrder.getId()))
                .body(convertDTO.convertToPurchaseOrderDTO(savedPurchaseOrder));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PurchaseOrderDTO updatedPurchaseOrderDTO){

        //Buscar la orden de compra
        Optional<PurchaseOrder> optionalPurchaseOrder = purchaseOrderService.findById(id);

        //Verificar si la referencia existe
        if(optionalPurchaseOrder.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La orden de compra no existe");
        }

        //Obtener Orden de Compra del Optional
        PurchaseOrder existingPurchaseOrder = optionalPurchaseOrder.get();

        //Crear y asociar los detalles de compra
        if(updatedPurchaseOrderDTO.getDetails().isEmpty()){
            System.out.println("Eliminamos");
            //Si la lista de detalles esta vacia, eliminar todos los detalles asociados a la orden de compra
            List<PurchaseDetail> newDetails = new ArrayList<>();

            //Eliminar detalles que ya no estan presentes
            existingPurchaseOrder.setDetails(newDetails);
        } else {
            System.out.println("No Eliminamos");
            //Obtener detalles existentes de la orden de Compra
            List<PurchaseDetail> existingDetails = existingPurchaseOrder.getDetails();

            //Agregar los detalles existentes al mapa de verificacion
            Map<String, PurchaseDetail> purchaseDetailMap = new HashMap<>();
            for (PurchaseDetail existingDetail : existingDetails){
                Product existingProduct = existingDetail.getProduct();
                String existingProductName = existingProduct.getName();

                //Agregar Detalle existente al mapa
                purchaseDetailMap.put(existingProductName, existingDetail);
            }

            //Verificar y procesar los nuevos detalles del DTO
            ResponseEntity<?> detailDTO = hanldePurchaseDetails(updatedPurchaseOrderDTO, existingPurchaseOrder, purchaseDetailMap);
            if (detailDTO != null) return detailDTO;

            //Actualizar y guardar la referencia
            existingPurchaseOrder = purchaseOrderService.save(existingPurchaseOrder);
            //Si la lista de detalles no esta vacia, crear y asociar los nuevos detalles
            List<PurchaseDetail> purchaseDetails = new ArrayList<>(purchaseDetailMap.values());

            //Asociar los detalles a la referencia y guardarlos
            existingPurchaseOrder.setDetails(purchaseDetails);
        }

        existingPurchaseOrder = purchaseOrderService.save(existingPurchaseOrder);
        return ResponseEntity.ok(convertDTO.convertToPurchaseOrderDTO(existingPurchaseOrder));
    }

    @Nullable
    private ResponseEntity<?> hanldePurchaseDetails(PurchaseOrderDTO updatePurchaseOrderDTO, PurchaseOrder existingPurchaseOrder, Map<String, PurchaseDetail> productDetailsMap){
        for (PurchaseDetailDTO detailDTO : updatePurchaseOrderDTO.getDetails()){
            Product product = productService.findByName(detailDTO.getProduct());
            if (product == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El producto " + detailDTO.getProduct() + " en los detalles no existe");
            }

            // Verificar si ya existe un detalle para el mismo producto
            String productName = detailDTO.getProduct();
            PurchaseDetail existingDetail = productDetailsMap.get(productName);
            if (existingDetail != null){
                // Sumar la cantidad al detalle existente
                existingDetail.setQuantity(existingDetail.getQuantity() + detailDTO.getQuantity());
            } else {
                // Crear un nuevo detalle y agregarlo al mapa
                PurchaseDetail newDetail = convertDTO.converToPurchaseDetail(detailDTO, existingPurchaseOrder);
                newDetail.setProduct(product);
                productDetailsMap.put(productName, newDetail);
            }
        }
        return null;
    }
}
