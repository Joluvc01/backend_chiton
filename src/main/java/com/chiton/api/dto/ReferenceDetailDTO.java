package com.chiton.api.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class ReferenceDetailDTO implements Serializable {

    final Long id;
    final String product;
    Double quantity;
}
