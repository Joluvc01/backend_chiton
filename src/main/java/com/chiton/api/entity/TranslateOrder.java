package com.chiton.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "translateOrder")
public class TranslateOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "production_id")
    private ProductionOrder productionOrder;

    @Temporal(TemporalType.DATE)
    @Column(name = "generation_date", updatable = false)
    @CreatedDate
    private LocalDate generationDate;

}
