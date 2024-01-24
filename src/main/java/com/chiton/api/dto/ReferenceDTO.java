package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class ReferenceDTO implements Serializable {

    final Long id;
    final String description;
    final String image;
    final List<ReferenceDetailDTO> details;
}
