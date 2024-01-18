package com.chiton.api.controller;

import com.chiton.api.dto.RoleDTO;
import com.chiton.api.entity.Role;
import com.chiton.api.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping()
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        return roleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody RoleDTO roleDTO) {
        // Verificar si el rol ya existe
        Role existingRole = roleService.findByName(roleDTO.getName());

        if (existingRole != null) {
            // Si el rol ya existe, devolver un mensaje indicando el conflicto
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El rol ya existe");
        }

        // Convierte el RoleDTO a Role
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setStatus(roleDTO.getStatus());

        // Guarda el rol en la base de datos
        Role savedRole = roleService.save(role);

        // Devuelve la respuesta con el rol creado
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRole);
    }


    @PutMapping("/update/{rolId}")
    public ResponseEntity<?> update(@PathVariable Long rolId, @RequestBody RoleDTO roleDTO) {
        // Verifica si el rol con el ID especificado existe en la base de datos
        Optional<Role> optionalRole = roleService.findById(rolId);

        if (optionalRole.isPresent()) {
                Role existingRole = optionalRole.get();

            // Actualiza los atributos del rol con los valores del DTO
            existingRole.setName(roleDTO.getName());
            existingRole.setStatus(roleDTO.getStatus());

            // Guarda el rol actualizado en la base de datos
            Role updatedRole = roleService.save(existingRole);

            // Devuelve la respuesta con el rol actualizado
            return ResponseEntity.ok(updatedRole);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrado con ID: " + rolId);
        }
    }

    @DeleteMapping("/delete/{rolId}")
    public ResponseEntity<?> delete(@PathVariable Long rolId) {
        // Verifica si el rol con el ID especificado existe en la base de datos
        Optional<Role> optionalRole = roleService.findById(rolId);

        if (optionalRole.isPresent()) {
            // Elimina el rol con el ID especificado
            roleService.deleteById(rolId);

            // Devuelve la respuesta con el mensaje de eliminación exitosa
            return ResponseEntity.ok("Rol eliminada con ID: " + rolId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rol no encontrada con ID: " + rolId);
        }
    }


}
