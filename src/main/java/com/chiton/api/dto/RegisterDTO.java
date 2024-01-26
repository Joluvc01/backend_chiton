package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class RegisterDTO implements Serializable {

    final String username;
    final String password;
    final String firstname;
    final String lastname;
    final Boolean status;
    final String role;
}