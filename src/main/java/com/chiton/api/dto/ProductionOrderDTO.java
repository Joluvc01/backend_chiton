package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductionOrderDTO implements Serializable {

    final Long id;
    final String customer;
    final LocalDate generationDate;
    final LocalDate deadline;
    final List<ProductionDetailDTO> details;
}
