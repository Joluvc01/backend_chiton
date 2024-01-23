package com.chiton.api.dto;

import com.chiton.api.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO implements Serializable {

    final Long id;
    final String username;
    final String firstname;
    final String lastname;
    final String role;
}