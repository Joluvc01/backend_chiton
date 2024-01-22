package com.chiton.api.service;

import com.chiton.api.entity.Reference;
import com.chiton.api.repository.ReferenceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
    public List<Reference> findAllByCustomerName(String customer) {
        return referenceRepository.findAllByCustomerName(customer);
    }

    @Override
    public Reference save(Reference reference) {
        return referenceRepository.save(reference);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Reference reference = referenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reference not found with id: " + id));

        // Antes de eliminar la referencia principal, eliminamos los detalles asociados
        if (reference.getDetail() != null) {
            reference.getDetail().forEach(detail -> detail.setReference(null));
            reference.getDetail().clear();
        }

        referenceRepository.delete(reference);
    }
}

