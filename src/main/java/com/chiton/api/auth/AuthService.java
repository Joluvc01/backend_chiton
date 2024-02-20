package com.chiton.api.auth;

import com.chiton.api.jwt.JwtService;
import com.chiton.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;



    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails user=userRepository.findByUsername(request.getUsername()).orElseThrow();
        String role= user.getAuthorities().toString();
        String token=jwtService.getToken(user);
        return AuthResponse.builder()
                .token(token)
                .role(this.cleanRole(role))
                .build();

    }

    public String cleanRole(String role) {
        if (role != null) {
            role = role.replace("[", "").replace("]", "");
        }
        return role;
    }
}
