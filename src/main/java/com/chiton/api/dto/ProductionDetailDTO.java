package com.chiton.api.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class ProductionDetailDTO implements Serializable {

    final Long id;
    final String reference;
    Double quantity;
}
