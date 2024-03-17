package com.chiton.api.config;

import com.chiton.api.entity.Role;
import com.chiton.api.entity.User;
import com.chiton.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminUserInitializer(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userService.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setFirstname("Admin");
            adminUser.setLastname("Admin");
            adminUser.setStatus("Activado");
            adminUser.setRole(Role.GERENCIA);
            userService.save(adminUser);
            System.out.println("Admin user created successfully.");
        }
    }
}