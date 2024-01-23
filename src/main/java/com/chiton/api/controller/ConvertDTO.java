package com.chiton.api.controller;

import com.chiton.api.dto.*;
import com.chiton.api.entity.*;
import com.chiton.api.service.ProductService;
import com.chiton.api.service.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConvertDTO {

    @Autowired
    private ProductService productService;

    public ProductDTO convertToProductDTO(Product product) {
        String category = (product.getName() != null) ? product.getCategory().getName() : null;
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
                ? reference.getDetail().stream().map(this::convertToReferenceDetailDTO).toList()
                : null;

        return new ReferenceDTO(
                reference.getId(),
                customer,
                reference.getDescription(),
                reference.getImage(),
                details
        );
    }

    public ReferenceDetailDTO convertToReferenceDetailDTO(ReferenceDetail detail) {
        String product = (detail.getProduct() != null) ? detail.getProduct().getName() : null;

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

    public List<ReferenceDetail> convertToReferenceDetailList(List<ReferenceDetailDTO> detailDTOList, Reference reference) {
        List<ReferenceDetail> referenceDetails = new ArrayList<>();

        for (ReferenceDetailDTO detailDTO : detailDTOList) {
            Product product = productService.findByName(detailDTO.getProduct());

            if (product != null) {
                ReferenceDetail referenceDetail = convertToReferenceDetail(detailDTO, reference);
                referenceDetail.setProduct(product);
                referenceDetails.add(referenceDetail);
            }
        }

        return referenceDetails;
    }


    public PurchaseOrderDTO convertToPurchaseOrderDTO(PurchaseOrder purchaseOrder){
        List<PurchaseDetailDTO> details = purchaseOrder.getDetails() != null
                ? purchaseOrder.getDetails().stream().map(this::convertToPurchaseDetailDTO).toList()
                : null;

        return new PurchaseOrderDTO(
                purchaseOrder.getId(),
                purchaseOrder.getGeneration_date(),
                details
        );
    }

    public PurchaseDetailDTO convertToPurchaseDetailDTO(PurchaseDetail detail){
        String product = (detail.getProduct() != null) ? detail.getProduct().getName() : null;

        return new PurchaseDetailDTO(
                detail.getId(),
                product,
                detail.getQuantity()
        );
    }

    public PurchaseDetail converToPurchaseDetail (PurchaseDetailDTO detailDTO, PurchaseOrder purchaseOrder){
        PurchaseDetail purchaseDetail = new PurchaseDetail();
        purchaseDetail.setProduct(productService.findByName(detailDTO.getProduct()));
        purchaseDetail.setQuantity(detailDTO.getQuantity());
        purchaseDetail.setPurchase_order(purchaseOrder);
        return purchaseDetail;
    }
}
