package com.chiton.api.auth;

import com.chiton.api.dto.UserDTO;
import com.chiton.api.entity.Role;
import com.chiton.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    String token;
    UserDTO user;
}

