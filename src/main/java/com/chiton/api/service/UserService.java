package com.chiton.api.service;

import com.chiton.api.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    public List<User> findAll();
    Optional<User> findByUsername(String username);
    public Optional<User> findById(Long id);
    public User save(User user);
}