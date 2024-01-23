package com.chiton.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "production_order")
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Temporal(TemporalType.DATE)
    @Column(name = "generation_date", nullable = false, updatable = false)
    @CreatedDate
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date generation_date;

    @Temporal(TemporalType.DATE)
    @Column(name = "deadline", nullable = false)
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date deadline;

    @OneToMany(mappedBy = "production_order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionDetail> details;
}
