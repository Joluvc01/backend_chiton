package com.chiton.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public enum Role {
    GERENCIA,
    PRODUCCION,
    ALMACEN,
    DISENIO
}

