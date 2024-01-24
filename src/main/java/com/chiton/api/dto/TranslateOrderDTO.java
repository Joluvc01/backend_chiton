package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TranslateOrderDTO implements Serializable {

    final Long id;
    final Long productionOrder;
    final LocalDate generationDate;
}
