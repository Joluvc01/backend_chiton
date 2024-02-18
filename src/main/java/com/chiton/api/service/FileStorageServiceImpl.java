package com.chiton.api.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Service
public class FileStorageServiceImpl  implements  FileStorageService{

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String storeFile(Long id, MultipartFile file) {
        try {
            // Construir el nombre de archivo utilizando el ID y la extensión ".jpg"
            String newFileName = id + ".jpg";

            // Obtener la ruta del archivo existente (si existe)
            Path existingFilePath = Paths.get(uploadDir + File.separator + newFileName);

            // Verificar si el archivo existente
            if (Files.exists(existingFilePath)) {
                // Eliminar el archivo existente
                Files.delete(existingFilePath);
            }

            // Guardar el nuevo archivo
            Path copyLocation = Paths.get(uploadDir + File.separator + newFileName);
            Files.copy(file.getInputStream(), copyLocation);

            return newFileName;
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo guardar la imagen", ex);
        }
    }

}
