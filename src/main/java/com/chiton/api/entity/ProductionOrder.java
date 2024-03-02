package com.chiton.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "productionOrder")
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "generationDate", nullable = false, updatable = false)
    private LocalDate generationDate;

    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @NotNull
    private String status;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionDetail> details;

    @OneToOne(orphanRemoval = true)
    private TranslateOrder translateOrder;
}
