package com.chiton.api.controller;

import com.chiton.api.dto.RegisterDTO;
import com.chiton.api.dto.UserDTO;
import com.chiton.api.entity.Role;
import com.chiton.api.entity.User;
import com.chiton.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private ConvertDTO convertDTO;

    @GetMapping()
    public ResponseEntity<?> findAll() {
        List<User> users = userService.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(convertDTO::convertToUserDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(convertDTO.convertToUserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody RegisterDTO registerDTO) {
        Optional<User> existingUser = userService.findByUsername(registerDTO.getUsername());

        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya está en uso");
        }

        User newuser = new User();
        newuser.setUsername(registerDTO.getUsername());
        newuser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        newuser.setFirstname(registerDTO.getFirstname());
        newuser.setLastname(registerDTO.getLastname());
        newuser.setRole(Role.valueOf(registerDTO.getRole()));

        User savedUser = userService.save(newuser);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertDTO.convertToUserDTO(savedUser));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> update(@PathVariable Long userId, @RequestBody RegisterDTO registerDTO) {
        Optional<User> optionalUser = userService.findById(userId);

        if (optionalUser.isPresent()) {
            Optional<User> dupUser = userService.findByUsername(registerDTO.getUsername());

            if (dupUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya está en uso");
            } else {
                User existingUser = optionalUser.get();
                existingUser.setUsername(registerDTO.getUsername());
                existingUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
                existingUser.setFirstname(registerDTO.getFirstname());
                existingUser.setLastname(registerDTO.getLastname());
                existingUser.setRole(Role.valueOf(registerDTO.getRole()));

                User updatedUser = userService.save(existingUser);
                return ResponseEntity.ok(convertDTO.convertToUserDTO(updatedUser));
            }

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> delete(@PathVariable Long userId) {
        Optional<User> optionalUser = userService.findById(userId);

        if (optionalUser.isPresent()) {
            userService.deleteById(userId);
            return ResponseEntity.ok("Usuario eliminado con ID: " + userId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado con ID: " + userId);
        }
    }

}
