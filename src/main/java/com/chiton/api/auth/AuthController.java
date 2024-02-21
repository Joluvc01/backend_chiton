package com.chiton.api.auth;

import com.chiton.api.entity.User;
import com.chiton.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String status = userRepository.findByUsername(request.getUsername()).orElseThrow().getStatus();
        if (Objects.equals(status, "Activado")){
            return ResponseEntity.ok(authService.login(request));
        }
        else {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Este usuario no se encuentra activo");
        }
    }
}
