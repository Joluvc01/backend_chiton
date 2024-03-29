package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor

public class CustomerDTO implements Serializable {

    final Long id;
    final String name;
    final String ruc;
    final String contactNumber;
    final String email;
    final String status;
}
