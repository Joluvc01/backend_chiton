package com.chiton.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PurchaseOrderDTO implements Serializable{

    final Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    final Date generation_date;
    final List<PurchaseDetailDTO> details;
}
