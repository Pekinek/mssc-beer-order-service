package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.web.model.BeerDto;

import java.util.Optional;

public interface BeerService {

    Optional<BeerDto> getBeerDto(String beerUpc);
}
