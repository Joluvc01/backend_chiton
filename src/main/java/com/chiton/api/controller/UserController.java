package com.chiton.api.controller;

import com.chiton.api.dto.RegisterDTO;
import com.chiton.api.dto.UserDTO;
import com.chiton.api.entity.Category;
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
                .toList();
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(convertDTO.convertToUserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping()
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
        newuser.setStatus(true);
        newuser.setRole(Role.valueOf(registerDTO.getRole()));

        User savedUser = userService.save(newuser);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertDTO.convertToUserDTO(savedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RegisterDTO registerDTO) {
        Optional<User> optionalUser = userService.findById(id);

        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setUsername(registerDTO.getUsername());
            existingUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            existingUser.setFirstname(registerDTO.getFirstname());
            existingUser.setLastname(registerDTO.getLastname());
            existingUser.setStatus(registerDTO.getStatus());
            existingUser.setRole(Role.valueOf(registerDTO.getRole()));

            User updatedUser = userService.save(existingUser);
            return ResponseEntity.ok(convertDTO.convertToUserDTO(updatedUser));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        Optional<User> optionalUser = userService.findById(id);

        if(optionalUser.isPresent()){
            userService.deleteById(id);
            return ResponseEntity.ok("Usuario eliminado");
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrada");
        }
    }
}