package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class TranslateOrderDTO implements Serializable {

    final Long id;
    final Long productionOrder;
    final LocalDate generationDate;
    final Boolean completed;
}
