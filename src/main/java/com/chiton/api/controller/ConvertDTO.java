package com.chiton.api.controller;

import com.chiton.api.dto.*;
import com.chiton.api.entity.Product;
import com.chiton.api.entity.Reference;
import com.chiton.api.entity.ReferenceDetail;
import com.chiton.api.entity.User;
import com.chiton.api.service.ProductService;
import com.chiton.api.service.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConvertDTO {

    @Autowired
    private ProductService productService;

    public ProductDTO convertToProductDTO(Product product) {
        String category = product.getCategory().getName();
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

    public ReferenceDTO convertToReferenceDTO(Reference reference) {
        String customer = reference.getCustomer().getName();

        List<ReferenceDetailDTO> details = reference.getDetail() != null
                ? reference.getDetail().stream().map(this::convertToReferenceDetailDTO).collect(Collectors.toList())
                : null;

        return new ReferenceDTO(
                reference.getId(),
                customer,
                reference.getDescription(),
                reference.getImage(),
                details
        );
    }

    private ReferenceDetailDTO convertToReferenceDetailDTO(ReferenceDetail detail) {
        String product = detail.getProduct().getName();

        return new ReferenceDetailDTO(
                detail.getId(),
                product,
                detail.getQuantity()
        );
    }

    public ReferenceDetail convertToReferenceDetail(ReferenceDetailDTO detailDTO, Reference reference) {
        ReferenceDetail referenceDetail = new ReferenceDetail();
        referenceDetail.setProduct(productService.findByName(detailDTO.getProduct()));
        referenceDetail.setQuantity(detailDTO.getQuantity());
        referenceDetail.setReference(reference);
        return referenceDetail;
    }

}
