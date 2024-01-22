package com.chiton.api.repository;

import com.chiton.api.entity.Reference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferenceRepository extends JpaRepository<Reference,Long> {
    List<Reference> findAllByCustomerName(String customer);
}
