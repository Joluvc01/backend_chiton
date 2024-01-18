package com.chiton.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {

    final Long id;
    final String username;
    final String password;
    final String role;
    final Boolean status;
}
