package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PurchaseOrderDTO implements Serializable{

    final Long id;
    final LocalDate generationDate;
    final String statusFech;
    final List<PurchaseDetailDTO> details;
}
