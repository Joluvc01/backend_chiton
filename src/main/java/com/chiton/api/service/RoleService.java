package com.chiton.api.service;

import com.chiton.api.entity.Role;
import java.util.List;
import java.util.Optional;

public interface RoleService {

    public List<Role> findAll();

    public Role findByName(String name);

    public Optional<Role> findById(Long id);

    public Role save(Role role);

    public void deleteById(Long id);
}
