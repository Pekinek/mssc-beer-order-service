package com.mmocek.commons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeallocateOrderRequest {

    static final long serialVersionUID = 6506959339043449927L;

    private BeerOrderDto beerOrderDto;
}