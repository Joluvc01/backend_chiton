package com.chiton.api.controller;

import com.chiton.api.entity.Customer;
import com.chiton.api.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        Optional<Customer> customer = customerService.findById(id);
        if (customer.isPresent()){
            return ResponseEntity.status(HttpStatus.OK).body(customer);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Customer customer) {
        Customer existingCustomer = customerService.findByName(customer.getName());
        if (existingCustomer != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El cliente ya existe");
        }
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
                customerService.save(customer);
                return ResponseEntity.status(HttpStatus.OK).body(customer);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<Customer> optionalCustomer = customerService.findById(id);
        if (optionalCustomer.isPresent()) {
            customerService.deleteById(id);
            return ResponseEntity.ok("Cliente eliminado con ID: " + id);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado con ID: " + id);
        }
    }
}
