package com.chiton.api.controller;

import com.chiton.api.dto.TranslateOrderDTO;
import com.chiton.api.entity.ProductionOrder;
import com.chiton.api.entity.TranslateOrder;
import com.chiton.api.service.ProductionOrderService;
import com.chiton.api.service.TranslateOrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/translateOrders")
public class TranslateOrderController {

    @Autowired
    private TranslateOrderService translateOrderService;

    @Autowired
    private ProductionOrderService productionOrderService;

    @Autowired
    private ConvertDTO convertDTO;

    @GetMapping()
    public ResponseEntity<?> findAll(){
        List<TranslateOrder> translateOrders = translateOrderService.findAll();
        List<TranslateOrderDTO> translateOrderDTOS = translateOrders.stream()
                .map(convertDTO::convertToTranslateOrderDTO)
                .toList();
        return ResponseEntity.ok(translateOrderDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        return translateOrderService.findById(id).map(translateOrder -> ResponseEntity.ok(convertDTO.convertToTranslateOrderDTO(translateOrder)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody TranslateOrderDTO translateOrderDTO){
        //Crear nueva orden de traslado
        TranslateOrder translateOrder = new TranslateOrder();

        //Buscar la orden de Produccion existente
        Optional<ProductionOrder> optionalProductionOrder = productionOrderService.findById(translateOrderDTO.getProductionOrder());

        //Verificar si exise la orden de Produccion
        if(optionalProductionOrder.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La orden de Produccion no existe");
        }

        //Obtener la orden de compra del Optional
        ProductionOrder existingprodOrder = optionalProductionOrder.get();

        translateOrder.setProductionOrder(existingprodOrder);
        LocalDate gendate = LocalDate.now(ZoneId.of("America/Lima"));
        translateOrder.setGenerationDate(gendate);
        translateOrder = translateOrderService.save(translateOrder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertDTO.convertToTranslateOrderDTO(translateOrder));
    }

    @Transactional
    @PutMapping("/update/{transalteId}")
    public ResponseEntity<?> update(@PathVariable Long transalteId,@RequestBody TranslateOrderDTO updatedTranslateOrderDTO){

        //Buscar la orden de traslado
        Optional<TranslateOrder> optionalTranslateOrder = translateOrderService.findById(transalteId);

        //Verifcar si la referencia Existe
        if(optionalTranslateOrder.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La orden de Traslado no existe");
        }

        //Obtener la ordend de traslado del optional
        TranslateOrder translateOrder = optionalTranslateOrder.get();

        //Buscar la orden de Produccion existente
        Optional<ProductionOrder> optionalProductionOrder = productionOrderService.findById(updatedTranslateOrderDTO.getProductionOrder());

        //Verificar si exise la orden de Produccion
        if(optionalProductionOrder.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La orden de Produccion no existe");
        }
        //Obtener la orden de compra del Optional
        ProductionOrder existingprodOrder = optionalProductionOrder.get();

        translateOrder.setProductionOrder(existingprodOrder);
        translateOrder = translateOrderService.save(translateOrder);

        return ResponseEntity.ok(convertDTO.convertToTranslateOrderDTO(translateOrder));
    }
}
