package com.chiton.api.service;

import com.chiton.api.entity.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerService {

    public List<Customer> findAll();

    public Customer findByName(String name);

    public Optional<Customer> findById(Long id);

    public Customer save(Customer customer);
}
