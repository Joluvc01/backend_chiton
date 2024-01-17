package com.chiton.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "reference")
public class Reference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "cusomer_id")
    private Customer customer;

    @NotNull
    @Size(min = 1, max = 20)
    private String descripcion;

    @NotNull
    private String image;

    @OneToMany(mappedBy = "reference", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Reference_detail> detail = new HashSet<>();



}
