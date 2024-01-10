package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class ProductDTO implements Serializable {

    final Long id;
    final String name;
    final String provider;
    final String color;
    final Double stock;
    final String category; //Cambio para recibir el nombre de la categoria
}
