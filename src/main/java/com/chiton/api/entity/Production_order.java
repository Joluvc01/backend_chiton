package com.chiton.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "production_order")
public class Production_order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Temporal(TemporalType.DATE)
    @Column(name = "generation_date", nullable = false, updatable = false)
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date generation_date;

    @Temporal(TemporalType.DATE)
    @Column(name = "deadline", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deadline;

    @OneToMany(mappedBy = "production_order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Production_detail> details = new HashSet<>();
}
