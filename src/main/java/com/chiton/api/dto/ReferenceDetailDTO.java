package com.chiton.api.dto;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReferenceDetailDTO implements Serializable {

    @EqualsAndHashCode.Include
    final Long id;

    @EqualsAndHashCode.Include
    final String product;

    final Double quantity;
}
