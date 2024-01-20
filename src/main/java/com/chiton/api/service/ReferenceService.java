package com.chiton.api.service;

import com.chiton.api.entity.Reference;

import java.util.List;
import java.util.Optional;

public interface ReferenceService {

    public List<Reference> findAll();

    public Optional<Reference> findById(Long id);

    public Reference findByCustomerName(String customer);

    public Reference save(Reference reference);

    public void deleteById(Long id);
}
