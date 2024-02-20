package com.chiton.api.controller;

import com.chiton.api.dto.CustomerDTO;
import com.chiton.api.entity.Category;
import com.chiton.api.entity.Customer;
import com.chiton.api.entity.ProductionOrder;
import com.chiton.api.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ConvertDTO convertDTO;

    @GetMapping()
    public ResponseEntity<?> findALL(){
        List<Customer> customers = customerService.findAll();
        List<CustomerDTO> customerDTOS = customers.stream()
                .map(convertDTO::convertToCustomerDTO)
                .toList();
        return ResponseEntity.ok(customerDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return customerService.findById(id)
                .map(customer -> ResponseEntity.ok(convertDTO.convertToCustomerDTO(customer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<?> create(@RequestBody CustomerDTO customerDTO) {
        Customer existingCustomer = customerService.findByName(customerDTO.getName());
        if (existingCustomer != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El cliente ya existe");
        }
        Customer newcustomer = new Customer();
        newcustomer.setName(customerDTO.getName());
        newcustomer.setRuc(customerDTO.getRuc());
        newcustomer.setContactNumber(customerDTO.getContactNumber());
        newcustomer.setEmail(customerDTO.getEmail());
        newcustomer.setStatus("Activado");
        customerService.save(newcustomer);
        CustomerDTO newCustomerDTO = convertDTO.convertToCustomerDTO(newcustomer);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCustomerDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) {
        Optional<Customer> optionalCustomer = customerService.findById(id);
        if (optionalCustomer.isPresent()) {
            Customer existingCustomer = optionalCustomer.get();
            existingCustomer.setName(customerDTO.getName());
            existingCustomer.setRuc(customerDTO.getRuc());
            existingCustomer.setContactNumber(customerDTO.getContactNumber());
            existingCustomer.setEmail(customerDTO.getEmail());
            Customer updatedCustomer = customerService.save(existingCustomer);
            CustomerDTO updatedCustomerDTO = convertDTO.convertToCustomerDTO(updatedCustomer);
            return ResponseEntity.ok(updatedCustomerDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
    }

    @PostMapping("/status/{id}")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        Optional<Customer> optionalCustomer = customerService.findById(id);

        if (optionalCustomer.isPresent()) {
            Customer existingCustomer = optionalCustomer.get();

            // Cambiar el estado de la categoría
            String currentStatus = existingCustomer.getStatus();
            String newStatus = currentStatus.equals("Activado") ? "Desactivado" : "Activado";
            existingCustomer.setStatus(newStatus);

            customerService.save(existingCustomer);
            String message = newStatus.equals("Activado") ? "Cliente activado" : "Cliente desactivado";
            return ResponseEntity.ok().body(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
    }

    @DeleteMapping("/{id}")
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
                relatedProd.put("OP", prodOrderIds);
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

    @GetMapping("/exist/{name}")
    public ResponseEntity<Boolean> checkCustomerExists(@PathVariable String name) {
        Customer cust = customerService.findByName(name);
        boolean exists = cust != null;
        return ResponseEntity.ok(exists);
    }
}
