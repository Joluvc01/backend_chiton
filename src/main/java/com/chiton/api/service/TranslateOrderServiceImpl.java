package com.chiton.api.service;

import com.chiton.api.entity.TranslateOrder;
import com.chiton.api.repository.TranslateOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TranslateOrderServiceImpl implements TranslateOrderService{

    @Autowired
    private TranslateOrderRepository translateOrderRepository;

    @Override
    public List<TranslateOrder> findAll() {
        return translateOrderRepository.findAll();
    }

    @Override
    public Optional<TranslateOrder> findById(Long id) {
        return translateOrderRepository.findById(id);
    }

    @Override
    public TranslateOrder save(TranslateOrder translateOrder) {
        return translateOrderRepository.save(translateOrder);
    }
}
