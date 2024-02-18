package com.chiton.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(Long id, MultipartFile file);
}
