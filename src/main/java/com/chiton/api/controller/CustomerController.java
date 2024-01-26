package com.chiton.api.controller;

import com.chiton.api.entity.Customer;
import com.chiton.api.entity.ProductionOrder;
import com.chiton.api.entity.User;
import com.chiton.api.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping()
    public ResponseEntity<?> findALL(){
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Customer customer) {
        Customer existingCustomer = customerService.findByName(customer.getName());
        if (existingCustomer != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El cliente ya existe");
        }
        customer.setStatus(true);
        Customer newcustomer = customerService.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(newcustomer);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Customer customer) {
        Optional<Customer> optionalCustomer = customerService.findById(id);
        if (optionalCustomer.isPresent()) {
            Customer existingCustomer = customerService.findByName(customer.getName());
            if (existingCustomer != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El cliente ya existe");
            } else {
                customer.setStatus(customer.getStatus());
                customerService.save(customer);
                return ResponseEntity.status(HttpStatus.OK).body(customer);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
    }

    @PatchMapping("/toggle-status/{id}")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        Optional<Customer> optionalCustomer = customerService.findById(id);

        if (optionalCustomer.isPresent()) {
            Customer existingCustomer = optionalCustomer.get();

            // Cambiar el estado del usuario
            existingCustomer.setStatus(!existingCustomer.getStatus()); // Invierte el estado actual
            customerService.save(existingCustomer);
            String message = existingCustomer.getStatus() ? "Cliente activado" : "Cliente desactivado";
            return ResponseEntity.ok().body(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        Optional<Customer> optionalCustomer = customerService.findById(id);

        if(optionalCustomer.isPresent()){
            Customer customer = optionalCustomer.get();

            //Obtener los id de las ordenes de produccion relacionadas
            List<Long> prodOrderIds = customer.getProductionOrder().stream()
                            .map(ProductionOrder::getId)
                            .toList();

            //Verificar si hay ordenes de produccion realcionados a este cliente
            if (!prodOrderIds.isEmpty()){
                Map<String, List<Long>> relatedProd = new HashMap<>();
                relatedProd.put("Ordenes de Produccion ID", prodOrderIds);
                return ResponseEntity.badRequest().body(relatedProd);
            }

            //Si no hay nada relaciona al cliente
            customerService.deleteById(id);
            return ResponseEntity.ok("Cliente eliminado");
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrada");
        }
    }
}
