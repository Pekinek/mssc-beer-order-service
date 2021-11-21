package com.mmocek.commons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateBeerOrderRequest {

    static final long serialVersionUID = 3170878019886420172L;

    private BeerOrderDto beerOrderDto;
}
