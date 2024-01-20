package com.chiton.api.repository;

import com.chiton.api.entity.Reference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferenceRepository extends JpaRepository<Reference,Long> {
    Reference findByCustomerName(String customer);
}
