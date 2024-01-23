package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class PurchaseDetailDTO implements Serializable {

    final Long id;
    final String product;
    Double quantity;

}
