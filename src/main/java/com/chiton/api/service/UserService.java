package com.chiton.api.service;

import com.chiton.api.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    public List<User> findAll();
    Optional<User> findByUsername(String username);
    public Optional<User> findById(Long id);
    public User save(User user);

    public void deleteById(Long id);
}
