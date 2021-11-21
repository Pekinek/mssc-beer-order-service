package com.mmocek.commons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    static final long serialVersionUID = 872260912633390159L;
    private UUID orderId;
    private boolean isValid;
}
