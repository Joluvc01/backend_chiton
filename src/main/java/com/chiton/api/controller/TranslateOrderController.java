package com.chiton.api.controller;

import com.chiton.api.dto.TranslateOrderDTO;
import com.chiton.api.entity.ProductionOrder;
import com.chiton.api.entity.TranslateOrder;
import com.chiton.api.service.ConvertDTO;
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
        return translateOrderService.findById(id)
                .map(translateOrder -> ResponseEntity.ok(convertDTO.convertToTranslateOrderDTO(translateOrder)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PostMapping()
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

        // Verificar si la orden de producción ya está asociada a un traslado
        if(existingprodOrder.getTranslateOrder() != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La orden de Produccion ya está asociada a un traslado");
        }

        translateOrder.setProductionOrder(existingprodOrder);
        LocalDate gendate = LocalDate.now(ZoneId.of("America/Lima"));
        translateOrder.setGenerationDate(gendate);
        translateOrder.setStatus("Incompleto");
        translateOrder = translateOrderService.save(translateOrder);
        existingprodOrder.setTranslateOrder(translateOrder);
        productionOrderService.save(existingprodOrder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertDTO.convertToTranslateOrderDTO(translateOrder));
    }


    @PostMapping("/status/{id}")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id){
        Optional<TranslateOrder> optionalTranslateOrder = translateOrderService.findById(id);

        if(optionalTranslateOrder.isPresent()){
            TranslateOrder existingTranslateOrder = optionalTranslateOrder.get();
            if (existingTranslateOrder.getStatus().equals("Completo")){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Esta Traslado ya fue completado");
            } else {
                existingTranslateOrder.setStatus("Completo");
                translateOrderService.save(existingTranslateOrder);
                return ResponseEntity.ok().body("Orden de Traslado completa.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Orden de Traslado no encontrada no encontrado");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        Optional<TranslateOrder> optionalTranslateOrder = translateOrderService.findById(id);

        if(optionalTranslateOrder.isPresent()){
            TranslateOrder translateOrder = optionalTranslateOrder.get();
            ProductionOrder productionOrder = translateOrder.getProductionOrder();

            productionOrder.setTranslateOrder(null);
            translateOrder.setProductionOrder(null);
            productionOrderService.save(productionOrder);
            translateOrderService.deleteById(id);
            return ResponseEntity.ok("Orden de traslado eliminado");
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Orden de traslado no encontrado");
        }
    }
}
