package com.chiton.api.controller;

import com.chiton.api.dto.*;
import com.chiton.api.entity.*;
import com.chiton.api.service.ProductService;
import com.chiton.api.service.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConvertDTO {

    @Autowired
    private ProductService productService;
    @Autowired
    private ReferenceService referenceService;

    public ProductDTO convertToProductDTO(Product product) {
        String category = (product.getName() != null) ? product.getCategory().getName() : null;
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getColor(),
                product.getStock(),
                product.getStatus(),
                category
        );
    }

    public UserDTO convertToUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getStatus(),
                user.getRole().name()
        );
    }

    public ReferenceDTO convertToReferenceDTO(Reference reference) {
        List<ReferenceDetailDTO> details = reference.getDetails() != null
                ? reference.getDetails().stream().map(this::convertToReferenceDetailDTO).toList()
                : null;

        return new ReferenceDTO(
                reference.getId(),
                reference.getDescription(),
                reference.getImage(),
                reference.getStatus(),
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

    public PurchaseOrderDTO convertToPurchaseOrderDTO(PurchaseOrder purchaseOrder){
        List<PurchaseDetailDTO> details = purchaseOrder.getDetails() != null
                ? purchaseOrder.getDetails().stream().map(this::convertToPurchaseDetailDTO).toList()
                : null;

        return new PurchaseOrderDTO(
                purchaseOrder.getId(),
                purchaseOrder.getGenerationDate(),
                purchaseOrder.getCompleted(),
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
        purchaseDetail.setPurchaseOrder(purchaseOrder);
        return purchaseDetail;
    }

    public ProductionOrderDTO convertToProductionOrderDTO(ProductionOrder productionOrder){
        String customer = (productionOrder.getCustomer() != null) ? productionOrder.getCustomer().getName() : null;
        List<ProductionDetailDTO> details = productionOrder.getDetails() != null
                ? productionOrder.getDetails().stream().map(this::convertToProductionDetailDTO).toList()
                : null;

        return new ProductionOrderDTO(
                productionOrder.getId(),
                customer,
                productionOrder.getGenerationDate(),
                productionOrder.getDeadline(),
                productionOrder.getCompleted(),
                details
        );
    }

    public ProductionDetailDTO convertToProductionDetailDTO(ProductionDetail detail){
        Long reference = (detail.getReference() != null) ? detail.getReference().getId() : null;

        return new ProductionDetailDTO(
                detail.getId(),
                reference,
                detail.getQuantity()
        );
    }

    public ProductionDetail convertToProductionDetail(ProductionDetailDTO detailDTO, ProductionOrder productionOrder){
        ProductionDetail productionDetail = new ProductionDetail();
        productionDetail.setReference(referenceService.findById(detailDTO.getReference()).get());
        productionDetail.setQuantity(detailDTO.getQuantity());
        productionDetail.setProductionOrder(productionOrder);
        return productionDetail;
    }

    public TranslateOrderDTO convertToTranslateOrderDTO(TranslateOrder translateOrder){
        Long prodId = (translateOrder.getProductionOrder() != null) ? translateOrder.getProductionOrder().getId() : null;

        return new TranslateOrderDTO(
                translateOrder.getId(),
                prodId,
                translateOrder.getGenerationDate(),
                translateOrder.getCompleted()
        );
    }
}
