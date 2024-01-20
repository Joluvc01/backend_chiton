package com.chiton.api.controller;

import com.chiton.api.dto.ProductDTO;
import com.chiton.api.dto.RegisterDTO;
import com.chiton.api.dto.UserDTO;
import com.chiton.api.entity.Product;
import com.chiton.api.entity.User;
import org.springframework.stereotype.Service;

@Service
public class ConvertDTO {
    public ProductDTO convertToProductDTO(Product product) {
        String category = product.getCategory() != null ? product.getCategory().getName() : null;
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getColor(),
                product.getStock(),
                category
        );
    }

    public UserDTO convertToUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getRole().name()
        );
    }

    public RegisterDTO convertToRegisterrDTO(User user) {
        return new RegisterDTO(
                user.getUsername(),
                user.getPassword(),
                user.getFirstname(),
                user.getLastname(),
                user.getRole().name()
        );
    }

}
