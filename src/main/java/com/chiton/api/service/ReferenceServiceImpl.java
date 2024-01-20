package com.chiton.api.service;

import com.chiton.api.entity.Reference;
import com.chiton.api.repository.ReferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReferenceServiceImpl implements ReferenceService{

    @Autowired
    private ReferenceRepository referenceRepository;

    @Override
    public List<Reference> findAll() {
        return referenceRepository.findAll();
    }

    @Override
    public Optional<Reference> findById(Long id) {
        return referenceRepository.findById(id);
    }

    @Override
    public Reference findByCustomerName(String customer) {
        return referenceRepository.findByCustomerName(customer);
    }

    @Override
    public Reference save(Reference reference) {
        return referenceRepository.save(reference);
    }

    @Override
    public void deleteById(Long id) {
        referenceRepository.deleteById(id);
    }
}
