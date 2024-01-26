package com.chiton.api.service;

import com.chiton.api.entity.TranslateOrder;
import java.util.List;
import java.util.Optional;

public interface TranslateOrderService {

    public List<TranslateOrder> findAll();

    public Optional<TranslateOrder> findById(Long id);

    public TranslateOrder save(TranslateOrder translateOrder);

}
